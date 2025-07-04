package com.vincenzo.shopping.payment.application.service

import com.vincenzo.shopping.common.kafka.KafkaMessage
import com.vincenzo.shopping.common.kafka.KafkaTopics
import com.vincenzo.shopping.payment.application.port.`in`.GetPaymentQuery
import com.vincenzo.shopping.payment.application.port.`in`.PaymentDetailCommand
import com.vincenzo.shopping.payment.application.port.`in`.ProcessPaymentCommand
import com.vincenzo.shopping.payment.application.port.`in`.ProcessPaymentUseCase
import com.vincenzo.shopping.payment.application.port.out.PaymentRepository
import com.vincenzo.shopping.payment.application.processor.*
import com.vincenzo.shopping.payment.domain.*
import kotlinx.coroutines.runBlocking
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PaymentService(
    private val paymentProcessors: List<PaymentProcessor>,
    private val paymentRepository: PaymentRepository,
    private val kafkaTemplate: KafkaTemplate<String, KafkaMessage<*>>
) : ProcessPaymentUseCase, GetPaymentQuery {
    
    private val processorMap: Map<PaymentMethod, PaymentProcessor> = 
        paymentProcessors.associateBy { it.getSupportedMethod() }
    
    override suspend fun processPayment(command: ProcessPaymentCommand): Payment {
        println("[PaymentService] 결제 처리 시작 - 주문: ${command.orderId}")
        
        // 결제 방법 결정 (단일 결제 or 복합 결제)
        val paymentMethod = if (command.paymentDetails.size > 1) {
            PaymentMethod.COMPOSITE
        } else {
            command.paymentDetails.first().method
        }
        
        // 결제 생성
        val payment = Payment(
            orderId = command.orderId,
            memberId = command.memberId,
            totalAmount = command.totalAmount,
            paymentMethod = paymentMethod,
            status = PaymentStatus.PROCESSING,
            paymentDetails = command.paymentDetails.map { detail ->
                PaymentDetail(
                    method = detail.method,
                    amount = detail.amount,
                    status = PaymentDetailStatus.PENDING,
                    metadata = detail.metadata
                )
            }
        )
        
        // 저장
        val savedPayment = paymentRepository.save(payment)
        
        try {
            val processedPayment = when (paymentMethod) {
                PaymentMethod.COMPOSITE -> processCompositePayment(savedPayment, command.paymentDetails)
                else -> processSinglePayment(savedPayment, command.paymentDetails.first())
            }
            
            // 결제 완료 이벤트 발행
            publishPaymentEvent(processedPayment)
            
            return processedPayment
        } catch (e: Exception) {
            println("[PaymentService] 결제 실패: ${e.message}")
            // 실패 처리
            val failedPayment = savedPayment.copy(
                status = PaymentStatus.FAILED,
                paymentDetails = savedPayment.paymentDetails.map { 
                    it.copy(status = PaymentDetailStatus.FAILED)
                }
            )
            paymentRepository.save(failedPayment)
            throw e
        }
    }
    
    private suspend fun processSinglePayment(
        payment: Payment,
        detailCommand: PaymentDetailCommand
    ): Payment {
        val processor = processorMap[detailCommand.method]
            ?: throw IllegalStateException("결제 프로세서를 찾을 수 없습니다: ${detailCommand.method}")
        
        // 결제 검증
        val validationResult = processor.validate(
            memberId = payment.memberId,
            amount = detailCommand.amount,
            metadata = detailCommand.metadata
        )
        
        if (!validationResult.isValid) {
            throw IllegalArgumentException(validationResult.message ?: "결제 검증 실패")
        }
        
        // 결제 처리
        val result = processor.process(
            orderId = payment.orderId,
            memberId = payment.memberId,
            amount = detailCommand.amount,
            metadata = detailCommand.metadata
        )
        
        if (!result.success) {
            throw RuntimeException("결제 처리 실패: ${result.message}")
        }
        
        // 결제 완료 상태 업데이트
        val completedPayment = payment.copy(
            status = PaymentStatus.COMPLETED,
            paymentDetails = payment.paymentDetails.map { detail ->
                if (detail.method == detailCommand.method) {
                    detail.copy(
                        status = PaymentDetailStatus.SUCCESS,
                        transactionId = result.transactionId
                    )
                } else {
                    detail
                }
            }
        )
        
        return paymentRepository.save(completedPayment)
    }
    
    private suspend fun processCompositePayment(
        payment: Payment,
        detailCommands: List<PaymentDetailCommand>
    ): Payment {
        // 복합결제 검증
        val mainMethod = detailCommands.find { it.method == PaymentMethod.PG_KPN }
            ?: throw IllegalArgumentException("복합결제는 PG를 메인 결제수단으로 해야 합니다.")
        
        val subMethods = detailCommands.filter { it.method != PaymentMethod.PG_KPN }
        
        val validationResult = PaymentRules.validateCompositePayment(
            mainMethod.method,
            subMethods.map { it.method }
        )
        
        if (!validationResult.isValid) {
            throw IllegalArgumentException(validationResult.message)
        }
        
        var updatedPayment = payment
        val processedDetails = mutableListOf<PaymentDetail>()
        
        try {
            // 서브 결제수단 먼저 처리 (포인트, 쿠폰)
            for (subDetail in subMethods) {
                val processor = processorMap[subDetail.method]
                    ?: throw IllegalStateException("결제 프로세서를 찾을 수 없습니다: ${subDetail.method}")
                
                val result = processor.process(
                    orderId = payment.orderId,
                    memberId = payment.memberId,
                    amount = subDetail.amount,
                    metadata = subDetail.metadata
                )
                
                if (!result.success) {
                    throw RuntimeException("${subDetail.method} 결제 실패: ${result.message}")
                }
                
                processedDetails.add(
                    PaymentDetail(
                        method = subDetail.method,
                        amount = subDetail.amount,
                        status = PaymentDetailStatus.SUCCESS,
                        transactionId = result.transactionId,
                        metadata = subDetail.metadata
                    )
                )
            }
            
            // 메인 결제수단 처리 (PG)
            val mainProcessor = processorMap[mainMethod.method]!!
            val mainResult = mainProcessor.process(
                orderId = payment.orderId,
                memberId = payment.memberId,
                amount = mainMethod.amount,
                metadata = mainMethod.metadata
            )
            
            if (!mainResult.success) {
                // 메인 결제 실패 시 서브 결제 롤백
                rollbackProcessedPayments(payment, processedDetails)
                throw RuntimeException("PG 결제 실패: ${mainResult.message}")
            }
            
            processedDetails.add(
                PaymentDetail(
                    method = mainMethod.method,
                    amount = mainMethod.amount,
                    status = PaymentDetailStatus.SUCCESS,
                    transactionId = mainResult.transactionId,
                    metadata = mainMethod.metadata
                )
            )
            
            // 결제 완료 상태 업데이트
            updatedPayment = payment.copy(
                status = PaymentStatus.COMPLETED,
                paymentDetails = processedDetails
            )
            
            return paymentRepository.save(updatedPayment)
            
        } catch (e: Exception) {
            // 실패 시 모든 결제 롤백
            rollbackProcessedPayments(payment, processedDetails)
            throw e
        }
    }
    
    private suspend fun rollbackProcessedPayments(
        payment: Payment,
        processedDetails: List<PaymentDetail>
    ) {
        for (detail in processedDetails) {
            try {
                val processor = processorMap[detail.method]!!
                processor.cancel(detail, "복합결제 실패로 인한 롤백")
            } catch (e: Exception) {
                println("[PaymentService] 롤백 실패 - ${detail.method}: ${e.message}")
            }
        }
    }
    
    private fun publishPaymentEvent(payment: Payment) {
        val event = PaymentCompletedEvent(
            paymentId = payment.id!!,
            orderId = payment.orderId,
            memberId = payment.memberId,
            totalAmount = payment.totalAmount,
            paymentMethod = payment.paymentMethod.name,
            status = payment.status.name
        )
        
        kafkaTemplate.send(
            KafkaTopics.PAYMENT_EVENTS,
            KafkaMessage(
                eventType = "PAYMENT_COMPLETED",
                payload = event
            )
        )
    }
    
    // GetPaymentQuery 구현
    @Transactional(readOnly = true)
    override fun getPaymentById(paymentId: Long): Payment? {
        return paymentRepository.findById(paymentId)
    }
    
    @Transactional(readOnly = true)
    override fun getPaymentByOrderId(orderId: Long): Payment? {
        return paymentRepository.findByOrderId(orderId)
    }
}

data class PaymentCompletedEvent(
    val paymentId: Long,
    val orderId: Long,
    val memberId: Long,
    val totalAmount: Long,
    val paymentMethod: String,
    val status: String
)
