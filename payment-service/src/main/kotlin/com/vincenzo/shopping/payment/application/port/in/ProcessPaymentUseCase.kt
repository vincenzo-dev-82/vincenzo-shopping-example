package com.vincenzo.shopping.payment.application.port.`in`

import com.vincenzo.shopping.payment.domain.Payment
import com.vincenzo.shopping.payment.domain.PaymentMethod

interface ProcessPaymentUseCase {
    suspend fun processPayment(command: ProcessPaymentCommand): Payment
}

data class ProcessPaymentCommand(
    val orderId: Long,
    val memberId: Long,
    val totalAmount: Long,
    val paymentDetails: List<PaymentDetailCommand>
)

data class PaymentDetailCommand(
    val method: PaymentMethod,
    val amount: Long,
    val metadata: Map<String, Any> = emptyMap()
)
