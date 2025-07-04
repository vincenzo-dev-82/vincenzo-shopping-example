package com.vincenzo.shopping.payment.application.port.`in`

import com.vincenzo.shopping.payment.domain.Payment
import com.vincenzo.shopping.payment.domain.PaymentMethod

/**
 * 결제 처리 Use Case
 */
interface ProcessPaymentUseCase {
    fun processPayment(command: ProcessPaymentCommand): Payment
}

/**
 * 결제 조회 Query
 */
interface GetPaymentQuery {
    fun getPayment(paymentId: Long): Payment?
    fun getPaymentByOrderId(orderId: Long): Payment?
}

/**
 * 결제 처리 커맨드
 */
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