package com.vincenzo.shopping.payment.domain

import java.time.LocalDateTime

data class Payment(
    val id: Long? = null,
    val orderId: Long,
    val paymentDetails: List<PaymentDetail>,
    val totalAmount: Long,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null
)

data class PaymentDetail(
    val id: Long? = null,
    val paymentId: Long? = null,
    val method: PaymentMethod,
    val amount: Long,
    val transactionId: String? = null,  // 외부 시스템의 트랜잭션 ID
    val status: PaymentDetailStatus = PaymentDetailStatus.PENDING,
    val metadata: Map<String, String> = emptyMap()  // 결제 수단별 추가 정보
)

enum class PaymentMethod {
    PG_KPN,         // PG 결제
    POINT,          // 캐시노트 포인트
    COUPON,         // 쿠폰
    BNPL            // 외상결제
}

enum class PaymentStatus {
    PENDING,        // 결제 대기
    PROCESSING,     // 결제 처리중
    COMPLETED,      // 결제 완료
    FAILED,         // 결제 실패
    CANCELLED,      // 결제 취소
    REFUNDED        // 환불됨
}

enum class PaymentDetailStatus {
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED,
    REFUNDED
}

// 결제 제약사항 검증을 위한 도메인 서비스
object PaymentValidator {
    fun validate(paymentDetails: List<PaymentDetail>, totalAmount: Long): ValidationResult {
        // 쿠폰은 단독 결제 불가
        if (paymentDetails.size == 1 && paymentDetails[0].method == PaymentMethod.COUPON) {
            return ValidationResult(false, "쿠폰은 단독으로 결제할 수 없습니다.")
        }
        
        // BNPL은 단독 결제만 가능
        val hasBnpl = paymentDetails.any { it.method == PaymentMethod.BNPL }
        if (hasBnpl && paymentDetails.size > 1) {
            return ValidationResult(false, "BNPL은 다른 결제 수단과 함께 사용할 수 없습니다.")
        }
        
        // 복합결제는 PG를 메인으로, 포인트/쿠폰 가능
        if (paymentDetails.size > 1) {
            val hasPg = paymentDetails.any { it.method == PaymentMethod.PG_KPN }
            if (!hasPg) {
                return ValidationResult(false, "복합결제 시 PG 결제가 포함되어야 합니다.")
            }
            
            val invalidMethods = paymentDetails.filter { 
                it.method != PaymentMethod.PG_KPN && 
                it.method != PaymentMethod.POINT && 
                it.method != PaymentMethod.COUPON 
            }
            if (invalidMethods.isNotEmpty()) {
                return ValidationResult(false, "복합결제는 PG, 포인트, 쿠폰만 가능합니다.")
            }
        }
        
        // 금액 검증
        val paymentSum = paymentDetails.sumOf { it.amount }
        if (paymentSum != totalAmount) {
            return ValidationResult(false, "결제 금액 합계가 주문 금액과 일치하지 않습니다.")
        }
        
        return ValidationResult(true)
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val message: String? = null
)