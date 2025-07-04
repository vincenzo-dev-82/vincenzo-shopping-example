package com.vincenzo.shopping.payment.domain

import java.time.LocalDateTime

data class Payment(
    val id: Long? = null,
    val orderId: Long,
    val memberId: Long,
    val totalAmount: Long,
    val paymentMethod: PaymentMethod,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val details: List<PaymentDetail> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null
)

data class PaymentDetail(
    val method: PaymentMethod,
    val amount: Long,
    val transactionId: String? = null,
    val metadata: Map<String, Any> = emptyMap()
)

enum class PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED,
    PARTIALLY_REFUNDED,
    REFUNDED
}

enum class PaymentMethod {
    PG_KPN,
    CASHNOTE_POINT,
    BNPL,
    COUPON,
    COMPOSITE  // 복합결제
}

// 결제 검증 규칙
object PaymentRules {
    fun validatePaymentMethod(method: PaymentMethod, amount: Long, memberPoint: Int): PaymentValidationResult {
        return when (method) {
            PaymentMethod.COUPON -> {
                PaymentValidationResult(
                    isValid = false,
                    message = "쿠폰은 단독 결제가 불가능합니다."
                )
            }
            PaymentMethod.CASHNOTE_POINT -> {
                if (memberPoint >= amount) {
                    PaymentValidationResult(isValid = true)
                } else {
                    PaymentValidationResult(
                        isValid = false,
                        message = "캐시노트 포인트가 부족합니다. 필요: $amount, 보유: $memberPoint"
                    )
                }
            }
            PaymentMethod.BNPL -> {
                // BNPL은 단독 결제만 가능
                PaymentValidationResult(isValid = true)
            }
            PaymentMethod.COMPOSITE -> {
                // 복합결제는 별도 검증 필요
                PaymentValidationResult(isValid = true)
            }
            else -> PaymentValidationResult(isValid = true)
        }
    }
    
    fun validateCompositePayment(
        mainMethod: PaymentMethod,
        subMethods: List<PaymentMethod>
    ): PaymentValidationResult {
        // 복합결제는 PG가 메인이어야 함
        if (mainMethod != PaymentMethod.PG_KPN) {
            return PaymentValidationResult(
                isValid = false,
                message = "복합결제의 메인 결제수단은 PG(KPN)이어야 합니다."
            )
        }
        
        // 서브 결제수단 검증
        val invalidSubMethods = subMethods.filter { 
            it != PaymentMethod.CASHNOTE_POINT && it != PaymentMethod.COUPON
        }
        
        if (invalidSubMethods.isNotEmpty()) {
            return PaymentValidationResult(
                isValid = false,
                message = "복합결제의 서브 결제수단은 캐시노트 포인트와 쿠폰만 가능합니다."
            )
        }
        
        return PaymentValidationResult(isValid = true)
    }
}

data class PaymentValidationResult(
    val isValid: Boolean,
    val message: String? = null
)