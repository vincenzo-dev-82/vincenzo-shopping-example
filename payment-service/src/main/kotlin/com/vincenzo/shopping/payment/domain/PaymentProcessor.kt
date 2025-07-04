package com.vincenzo.shopping.payment.domain

interface PaymentProcessor {
    fun process(paymentDetail: PaymentDetail): PaymentResult
    fun cancel(paymentDetail: PaymentDetail): PaymentResult
    fun refund(paymentDetail: PaymentDetail, refundAmount: Long): PaymentResult
    fun supports(method: PaymentMethod): Boolean
}

data class PaymentResult(
    val success: Boolean,
    val transactionId: String? = null,
    val message: String? = null,
    val metadata: Map<String, String> = emptyMap()
)