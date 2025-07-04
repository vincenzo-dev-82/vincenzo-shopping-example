package com.vincenzo.shopping.payment.domain

import java.time.LocalDateTime

data class Payment(
    val id: Long? = null,
    val orderId: Long,
    val memberId: Long,
    val totalAmount: Long,
    val paymentMethod: PaymentMethod,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val paymentDetails: List<PaymentDetail> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null
)

data class PaymentDetail(
    val method: PaymentMethod,
    val amount: Long,
    val transactionId: String? = null,
    val status: PaymentDetailStatus = PaymentDetailStatus.PENDING,
    val metadata: Map<String, Any> = emptyMap()
)

enum class PaymentDetailStatus {
    PENDING,
    SUCCESS,
    FAILED,
    CANCELLED
}

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
    POINT,  // CASHNOTE_POINT의 별칭
    BNPL,
    COUPON,
    COMPOSITE  // 복합결제
}