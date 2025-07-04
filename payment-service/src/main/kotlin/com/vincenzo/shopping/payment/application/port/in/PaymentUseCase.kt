package com.vincenzo.shopping.payment.application.port.`in`

import com.vincenzo.shopping.payment.domain.Payment
import com.vincenzo.shopping.payment.domain.PaymentMethod

interface ProcessPaymentUseCase {
    fun processPayment(command: ProcessPaymentCommand): Payment
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
    val metadata: Map<String, String> = emptyMap()
)

interface CancelPaymentUseCase {
    fun cancelPayment(paymentId: Long): Payment
}

interface RefundPaymentUseCase {
    fun refundPayment(command: RefundPaymentCommand): Payment
}

data class RefundPaymentCommand(
    val paymentId: Long,
    val refundDetails: List<RefundDetailCommand>
)

data class RefundDetailCommand(
    val paymentDetailId: Long,
    val refundAmount: Long
)

interface GetPaymentQuery {
    fun getPayment(paymentId: Long): Payment?
    fun getPaymentByOrderId(orderId: Long): Payment?
}
