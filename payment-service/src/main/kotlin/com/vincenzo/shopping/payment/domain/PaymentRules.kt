package com.vincenzo.shopping.payment.domain

/**
 * 결제 규칙 및 검증 로직
 */
object PaymentRules {
    
    data class ValidationResult(
        val isValid: Boolean,
        val message: String? = null
    )
    
    /**
     * 복합결제 검증
     * - PG(KPN)를 메인 결제수단으로 해야 함
     * - 서브 결제수단은 쿠폰, 캐시노트 포인트만 가능
     * - BNPL은 복합결제 불가
     */
    fun validateCompositePayment(
        mainMethod: PaymentMethod,
        subMethods: List<PaymentMethod>
    ): ValidationResult {
        // 메인 결제수단 검증
        if (mainMethod != PaymentMethod.PG_KPN) {
            return ValidationResult(
                isValid = false,
                message = "복합결제의 메인 결제수단은 PG여야 합니다."
            )
        }
        
        // 서브 결제수단 검증
        val invalidSubMethods = subMethods.filter { 
            it !in listOf(PaymentMethod.CASHNOTE_POINT, PaymentMethod.COUPON)
        }
        
        if (invalidSubMethods.isNotEmpty()) {
            return ValidationResult(
                isValid = false,
                message = "복합결제의 서브 결제수단은 포인트와 쿠폰만 가능합니다. 잘못된 결제수단: $invalidSubMethods"
            )
        }
        
        // 쿠폰 중복 검사
        val couponCount = subMethods.count { it == PaymentMethod.COUPON }
        if (couponCount > 1) {
            return ValidationResult(
                isValid = false,
                message = "쿠폰은 한 개만 사용 가능합니다."
            )
        }
        
        return ValidationResult(isValid = true)
    }
    
    /**
     * 단일 결제 검증
     * - 쿠폰은 단독 결제 불가
     * - BNPL은 단독 결제만 가능
     */
    fun validateSinglePayment(method: PaymentMethod): ValidationResult {
        return when (method) {
            PaymentMethod.COUPON -> ValidationResult(
                isValid = false,
                message = "쿠폰은 단독으로 결제할 수 없습니다."
            )
            else -> ValidationResult(isValid = true)
        }
    }
    
    /**
     * 결제 금액 검증
     */
    fun validatePaymentAmount(
        totalAmount: Long,
        paymentDetails: List<PaymentDetail>
    ): ValidationResult {
        val sum = paymentDetails.sumOf { it.amount }
        
        return if (sum == totalAmount) {
            ValidationResult(isValid = true)
        } else {
            ValidationResult(
                isValid = false,
                message = "결제 금액이 일치하지 않습니다. 주문금액: $totalAmount, 결제금액: $sum"
            )
        }
    }
}