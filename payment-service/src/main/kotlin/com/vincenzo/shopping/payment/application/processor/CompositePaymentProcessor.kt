package com.vincenzo.shopping.payment.application.processor

import com.vincenzo.shopping.payment.domain.PaymentDetail
import com.vincenzo.shopping.payment.domain.PaymentMethod
import com.vincenzo.shopping.payment.domain.PaymentRules
import org.springframework.stereotype.Component

@Component
class CompositePaymentProcessor(
    private val paymentProcessors: List<PaymentProcessor>
) : PaymentProcessor {
    
    private val processorMap: Map<PaymentMethod, PaymentProcessor> = 
        paymentProcessors.associateBy { it.getSupportedMethod() }
    
    override fun getSupportedMethod(): PaymentMethod = PaymentMethod.COMPOSITE
    
    override suspend fun process(
        orderId: Long,
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any>
    ): PaymentResult {
        println("[COMPOSITE] 복합결제 처리 시작 - 주문: $orderId, 총액: $amount")
        
        // 메타데이터에서 결제 계획 추출
        val paymentPlan = metadata["paymentPlan"] as? CompositePaymentPlan
            ?: return PaymentResult(
                success = false,
                message = "복합결제 계획이 필요합니다."
            )
        
        // 복합결제 규칙 검증
        val validationResult = PaymentRules.validateCompositePayment(
            mainMethod = paymentPlan.mainMethod,
            subMethods = paymentPlan.subPayments.map { it.method }
        )
        
        if (!validationResult.isValid) {
            return PaymentResult(
                success = false,
                message = validationResult.message
            )
        }
        
        // 총 결제 금액 검증
        val totalPlannedAmount = paymentPlan.mainAmount + 
            paymentPlan.subPayments.sumOf { it.amount }
        
        if (totalPlannedAmount != amount) {
            return PaymentResult(
                success = false,
                message = "결제 계획 금액($totalPlannedAmount)과 주문 금액($amount)이 일치하지 않습니다."
            )
        }
        
        val processedDetails = mutableListOf<PaymentDetail>()
        var totalProcessedAmount = 0L
        
        try {
            // 1. 서브 결제 수단 먼저 처리 (쿠폰, 포인트)
            for (subPayment in paymentPlan.subPayments) {
                val processor = processorMap[subPayment.method]
                    ?: continue
                
                val result = processor.process(
                    orderId = orderId,
                    memberId = memberId,
                    amount = subPayment.amount,
                    metadata = subPayment.metadata
                )
                
                if (!result.success) {
                    // 실패 시 이전 결제들 롤백
                    rollbackPayments(processedDetails)
                    return PaymentResult(
                        success = false,
                        message = "서브 결제 실패 (${subPayment.method}): ${result.message}"
                    )
                }
                
                processedDetails.add(
                    PaymentDetail(
                        method = subPayment.method,
                        amount = result.processedAmount,
                        transactionId = result.transactionId,
                        metadata = result.metadata + ("memberId" to memberId)
                    )
                )
                totalProcessedAmount += result.processedAmount
            }
            
            // 2. 메인 결제 수단 처리 (PG)
            val mainProcessor = processorMap[paymentPlan.mainMethod]
                ?: return PaymentResult(
                    success = false,
                    message = "메인 결제 프로세서를 찾을 수 없습니다."
                )
            
            val mainResult = mainProcessor.process(
                orderId = orderId,
                memberId = memberId,
                amount = paymentPlan.mainAmount,
                metadata = paymentPlan.mainMetadata
            )
            
            if (!mainResult.success) {
                // 메인 결제 실패 시 서브 결제들 롤백
                rollbackPayments(processedDetails)
                return PaymentResult(
                    success = false,
                    message = "메인 결제 실패 (${paymentPlan.mainMethod}): ${mainResult.message}"
                )
            }
            
            processedDetails.add(
                PaymentDetail(
                    method = paymentPlan.mainMethod,
                    amount = mainResult.processedAmount,
                    transactionId = mainResult.transactionId,
                    metadata = mainResult.metadata
                )
            )
            totalProcessedAmount += mainResult.processedAmount
            
            return PaymentResult(
                success = true,
                transactionId = "COMPOSITE-$orderId-${System.currentTimeMillis()}",
                message = "복합결제 완료",
                processedAmount = totalProcessedAmount,
                metadata = mapOf(
                    "paymentDetails" to processedDetails,
                    "mainMethod" to paymentPlan.mainMethod,
                    "subMethods" to paymentPlan.subPayments.map { it.method }
                )
            )
            
        } catch (e: Exception) {
            // 예외 발생 시 모든 결제 롤백
            rollbackPayments(processedDetails)
            return PaymentResult(
                success = false,
                message = "복합결제 처리 중 오류: ${e.message}"
            )
        }
    }
    
    override suspend fun cancel(
        paymentDetail: PaymentDetail,
        reason: String
    ): PaymentResult {
        println("[COMPOSITE] 복합결제 취소 시작 - 거래ID: ${paymentDetail.transactionId}")
        
        val details = paymentDetail.metadata["paymentDetails"] as? List<PaymentDetail>
            ?: return PaymentResult(
                success = false,
                message = "복합결제 상세 정보를 찾을 수 없습니다."
            )
        
        val cancelResults = mutableListOf<Pair<PaymentMethod, PaymentResult>>()
        var allSuccess = true
        
        // 모든 결제 수단 취소
        for (detail in details) {
            val processor = processorMap[detail.method]
            if (processor != null) {
                val cancelResult = processor.cancel(detail, reason)
                cancelResults.add(detail.method to cancelResult)
                if (!cancelResult.success) {
                    allSuccess = false
                }
            }
        }
        
        return PaymentResult(
            success = allSuccess,
            transactionId = "CANCEL-${paymentDetail.transactionId}",
            message = if (allSuccess) "복합결제 취소 완료" else "일부 결제 취소 실패",
            processedAmount = -paymentDetail.amount,
            metadata = mapOf(
                "cancelResults" to cancelResults
            )
        )
    }
    
    override suspend fun validate(
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any>
    ): ValidationResult {
        val paymentPlan = metadata["paymentPlan"] as? CompositePaymentPlan
            ?: return ValidationResult(
                isValid = false,
                message = "복합결제 계획이 필요합니다."
            )
        
        // 복합결제 규칙 검증
        val rulesValidation = PaymentRules.validateCompositePayment(
            mainMethod = paymentPlan.mainMethod,
            subMethods = paymentPlan.subPayments.map { it.method }
        )
        
        if (!rulesValidation.isValid) {
            return ValidationResult(
                isValid = false,
                message = rulesValidation.message
            )
        }
        
        // 각 결제 수단별 검증
        for (subPayment in paymentPlan.subPayments) {
            val processor = processorMap[subPayment.method]
            if (processor != null && subPayment.method != PaymentMethod.COUPON) {
                val validation = processor.validate(memberId, subPayment.amount, subPayment.metadata)
                if (!validation.isValid) {
                    return ValidationResult(
                        isValid = false,
                        message = "서브 결제 검증 실패 (${subPayment.method}): ${validation.message}"
                    )
                }
            }
        }
        
        return ValidationResult(
            isValid = true,
            message = "복합결제 가능"
        )
    }
    
    private suspend fun rollbackPayments(processedDetails: List<PaymentDetail>) {
        println("[COMPOSITE] 결제 롤백 시작 - ${processedDetails.size}건")
        
        for (detail in processedDetails.reversed()) {
            val processor = processorMap[detail.method]
            processor?.cancel(detail, "복합결제 실패로 인한 자동 취소")
        }
    }
}

data class CompositePaymentPlan(
    val mainMethod: PaymentMethod,
    val mainAmount: Long,
    val mainMetadata: Map<String, Any> = emptyMap(),
    val subPayments: List<SubPayment> = emptyList()
)

data class SubPayment(
    val method: PaymentMethod,
    val amount: Long,
    val metadata: Map<String, Any> = emptyMap()
)