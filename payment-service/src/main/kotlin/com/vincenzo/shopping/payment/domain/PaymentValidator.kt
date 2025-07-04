package com.vincenzo.shopping.payment.domain

/**
 * 결제 유효성 검사기
 * Strategy Pattern과 Chain of Responsibility Pattern을 활용
 */
class PaymentValidator(
    private val validationStrategies: List<PaymentValidationStrategy>
) {
    
    fun validate(
        paymentMethod: PaymentMethod,
        amount: Long,
        memberId: Long,
        metadata: Map<String, Any> = emptyMap()
    ): PaymentValidationResult {
        val request = PaymentValidationRequest(
            paymentMethod = paymentMethod,
            amount = amount,
            memberId = memberId,
            metadata = metadata
        )
        
        // 모든 검증 전략을 순차적으로 실행
        for (strategy in validationStrategies) {
            val result = strategy.validate(request)
            if (!result.isValid) {
                return result
            }
        }
        
        return PaymentValidationResult(
            isValid = true,
            message = "모든 검증을 통과했습니다"
        )
    }
    
    /**
     * 복합 결제 검증
     */
    fun validateCompositePayment(
        mainMethod: PaymentMethod,
        subMethods: List<PaymentMethod>,
        totalAmount: Long,
        memberId: Long
    ): PaymentValidationResult {
        // 복합결제 전용 검증
        val metadata = mapOf(
            "mainMethod" to mainMethod,
            "subMethods" to subMethods
        )
        
        return validate(
            paymentMethod = PaymentMethod.COMPOSITE,
            amount = totalAmount,
            memberId = memberId,
            metadata = metadata
        )
    }
}