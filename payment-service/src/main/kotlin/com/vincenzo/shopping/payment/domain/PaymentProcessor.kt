package com.vincenzo.shopping.payment.domain

// 결제 프로세서 인터페이스
interface PaymentProcessor {
    fun supports(method: PaymentMethod): Boolean
    fun process(paymentDetail: PaymentDetail): PaymentResult
    fun cancel(paymentDetail: PaymentDetail): PaymentResult
    fun refund(paymentDetail: PaymentDetail, refundAmount: Long): PaymentResult
}

data class PaymentResult(
    val success: Boolean,
    val transactionId: String? = null,
    val message: String? = null,
    val metadata: Map<String, String> = emptyMap()
)