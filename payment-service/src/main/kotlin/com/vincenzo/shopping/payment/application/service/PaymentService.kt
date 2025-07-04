package com.vincenzo.shopping.payment.application.service

import com.vincenzo.shopping.payment.application.port.`in`.*
import com.vincenzo.shopping.payment.application.port.out.PaymentRepository
import com.vincenzo.shopping.payment.domain.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val paymentProcessors: List<PaymentProcessor>
) : ProcessPaymentUseCase, CancelPaymentUseCase, RefundPaymentUseCase, GetPaymentQuery {
    
    override fun processPayment(command: ProcessPaymentCommand): Payment {
        // 1. 결제 상세 정보 생성
        val paymentDetails = command.paymentDetails.map { detail ->
            PaymentDetail(
                method = detail.method,
                amount = detail.amount,
                metadata = detail.metadata + mapOf(
                    "member_id" to command.memberId.toString(),
                    "order_id" to command.orderId.toString()
                )
            )
        }
        
        // 2. 결제 검증
        val validationResult = PaymentValidator.validate(paymentDetails, command.totalAmount)
        if (!validationResult.isValid) {
            throw IllegalArgumentException(validationResult.message)
        }
        
        // 3. 결제 생성
        var payment = Payment(
            orderId = command.orderId,
            paymentDetails = paymentDetails,
            totalAmount = command.totalAmount,
            status = PaymentStatus.PROCESSING
        )
        payment = paymentRepository.save(payment)
        
        // 4. 각 결제 수단별로 처리
        val processedDetails = mutableListOf<PaymentDetail>()
        val failedDetails = mutableListOf<PaymentDetail>()
        
        for (detail in payment.paymentDetails) {
            val processor = findProcessor(detail.method)
                ?: throw IllegalStateException("결제 프로세서를 찾을 수 없습니다: ${detail.method}")
            
            try {
                val result = processor.process(detail)
                
                if (result.success) {
                    processedDetails.add(
                        detail.copy(
                            paymentId = payment.id,
                            status = PaymentDetailStatus.COMPLETED,
                            transactionId = result.transactionId,
                            metadata = detail.metadata + result.metadata
                        )
                    )
                } else {
                    failedDetails.add(
                        detail.copy(
                            paymentId = payment.id,
                            status = PaymentDetailStatus.FAILED,
                            metadata = detail.metadata + mapOf("failure_reason" to (result.message ?: "Unknown"))
                        )
                    )
                    
                    // 실패 시 이전 결제들 롤백
                    rollbackPayments(processedDetails)
                    
                    throw PaymentProcessingException(result.message ?: "결제 처리 실패")
                }
            } catch (e: Exception) {
                // 롤백 처리
                rollbackPayments(processedDetails)
                throw e
            }
        }
        
        // 5. 결제 완료 처리
        val completedPayment = payment.copy(
            paymentDetails = processedDetails,
            status = PaymentStatus.COMPLETED,
            completedAt = LocalDateTime.now()
        )
        
        return paymentRepository.save(completedPayment)
    }
    
    override fun cancelPayment(paymentId: Long): Payment {
        val payment = paymentRepository.findById(paymentId)
            ?: throw IllegalArgumentException("결제를 찾을 수 없습니다: $paymentId")
        
        if (payment.status != PaymentStatus.COMPLETED) {
            throw IllegalStateException("완료된 결제만 취소할 수 있습니다.")
        }
        
        // 각 결제 수단별로 취소 처리
        val cancelledDetails = payment.paymentDetails.map { detail ->
            val processor = findProcessor(detail.method)
                ?: throw IllegalStateException("결제 프로세서를 찾을 수 없습니다: ${detail.method}")
            
            val result = processor.cancel(detail)
            if (!result.success) {
                throw PaymentProcessingException("결제 취소 실패: ${result.message}")
            }
            
            detail.copy(
                status = PaymentDetailStatus.CANCELLED,
                metadata = detail.metadata + mapOf("cancel_transaction_id" to (result.transactionId ?: ""))
            )
        }
        
        val cancelledPayment = payment.copy(
            paymentDetails = cancelledDetails,
            status = PaymentStatus.CANCELLED
        )
        
        return paymentRepository.save(cancelledPayment)
    }
    
    override fun refundPayment(command: RefundPaymentCommand): Payment {
        val payment = paymentRepository.findById(command.paymentId)
            ?: throw IllegalArgumentException("결제를 찾을 수 없습니다: ${command.paymentId}")
        
        if (payment.status != PaymentStatus.COMPLETED) {
            throw IllegalStateException("완료된 결제만 환불할 수 있습니다.")
        }
        
        // 환불 처리
        val refundedDetails = payment.paymentDetails.map { detail ->
            val refundCommand = command.refundDetails.find { it.paymentDetailId == detail.id }
            if (refundCommand != null) {
                val processor = findProcessor(detail.method)
                    ?: throw IllegalStateException("결제 프로세서를 찾을 수 없습니다: ${detail.method}")
                
                val result = processor.refund(detail, refundCommand.refundAmount)
                if (!result.success) {
                    throw PaymentProcessingException("환불 처리 실패: ${result.message}")
                }
                
                detail.copy(
                    status = PaymentDetailStatus.REFUNDED,
                    metadata = detail.metadata + mapOf(
                        "refund_transaction_id" to (result.transactionId ?: ""),
                        "refund_amount" to refundCommand.refundAmount.toString()
                    )
                )
            } else {
                detail
            }
        }
        
        // 전체 환불인지 부분 환불인지 확인
        val allRefunded = refundedDetails.all { it.status == PaymentDetailStatus.REFUNDED }
        
        val refundedPayment = payment.copy(
            paymentDetails = refundedDetails,
            status = if (allRefunded) PaymentStatus.REFUNDED else payment.status
        )
        
        return paymentRepository.save(refundedPayment)
    }
    
    @Transactional(readOnly = true)
    override fun getPayment(paymentId: Long): Payment? {
        return paymentRepository.findById(paymentId)
    }
    
    @Transactional(readOnly = true)
    override fun getPaymentByOrderId(orderId: Long): Payment? {
        return paymentRepository.findByOrderId(orderId)
    }
    
    private fun findProcessor(method: PaymentMethod): PaymentProcessor? {
        return paymentProcessors.find { it.supports(method) }
    }
    
    private fun rollbackPayments(processedDetails: List<PaymentDetail>) {
        processedDetails.forEach { detail ->
            try {
                val processor = findProcessor(detail.method)
                processor?.cancel(detail)
            } catch (e: Exception) {
                // 롤백 실패 로깅
                println("롤백 실패: ${detail.method}, ${e.message}")
            }
        }
    }
}

class PaymentProcessingException(message: String) : RuntimeException(message)