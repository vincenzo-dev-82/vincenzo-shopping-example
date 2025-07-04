package com.vincenzo.shopping.payment.application.port.`in`

import com.vincenzo.shopping.payment.domain.Payment
import com.vincenzo.shopping.payment.domain.PaymentMethod

// UseCase 인터페이스들
interface ProcessPaymentUseCase {
    fun processPayment(command: ProcessPaymentCommand): Payment
}

interface GetPaymentQuery {
    fun getPayment(paymentId: Long): Payment?
    fun getPaymentByOrderId(orderId: Long): Payment?
}

// Command 객체들
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