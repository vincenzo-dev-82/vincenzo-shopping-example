package com.vincenzo.shopping.payment.domain

/**
 * 결제 검증 유틸리티
 */
object PaymentValidator {
    
    fun validatePaymentDetails(
        paymentDetails: List<PaymentDetail>,
        totalAmount: Long
    ): PaymentRules.ValidationResult {
        // 총 금액 검증
        val detailsSum = paymentDetails.sumOf { it.amount }
        if (detailsSum != totalAmount) {
            return PaymentRules.ValidationResult(
                isValid = false,
                message = "결제 상세 금액의 합($detailsSum)이 총 금액($totalAmount)과 일치하지 않습니다."
            )
        }
        
        // 쿠폰 단독 결제 검증
        if (paymentDetails.size == 1 && paymentDetails.first().method == PaymentMethod.COUPON) {
            return PaymentRules.ValidationResult(
                isValid = false,
                message = "쿠폰은 단독 결제가 불가능합니다."
            )
        }
        
        // BNPL 복합 결제 검증
        if (paymentDetails.any { it.method == PaymentMethod.BNPL } && paymentDetails.size > 1) {
            return PaymentRules.ValidationResult(
                isValid = false,
                message = "BNPL은 단독 결제만 가능합니다."
            )
        }
        
        // 복합 결제 검증
        if (paymentDetails.size > 1) {
            val hasMainMethod = paymentDetails.any { it.method == PaymentMethod.PG_KPN }
            if (!hasMainMethod) {
                return PaymentRules.ValidationResult(
                    isValid = false,
                    message = "복합 결제는 PG(KPN)을 메인 결제수단으로 포함해야 합니다."
                )
            }
            
            val invalidSubMethods = paymentDetails
                .filter { it.method != PaymentMethod.PG_KPN }
                .any { it.method != PaymentMethod.CASHNOTE_POINT && it.method != PaymentMethod.COUPON }
            
            if (invalidSubMethods) {
                return PaymentRules.ValidationResult(
                    isValid = false,
                    message = "복합 결제의 서브 결제수단은 포인트와 쿠폰만 가능합니다."
                )
            }
        }
        
        return PaymentRules.ValidationResult(isValid = true)
    }
}