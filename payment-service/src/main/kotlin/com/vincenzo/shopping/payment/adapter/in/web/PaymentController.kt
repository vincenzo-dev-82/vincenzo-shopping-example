package com.vincenzo.shopping.payment.adapter.`in`.web

import com.vincenzo.shopping.payment.application.port.`in`.*
import com.vincenzo.shopping.payment.domain.Payment
import com.vincenzo.shopping.payment.domain.PaymentMethod
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/payments")
class PaymentController(
    private val processPaymentUseCase: ProcessPaymentUseCase,
    private val cancelPaymentUseCase: CancelPaymentUseCase,
    private val refundPaymentUseCase: RefundPaymentUseCase,
    private val getPaymentQuery: GetPaymentQuery
) {
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun processPayment(@RequestBody request: ProcessPaymentRequest): PaymentResponse {
        val command = ProcessPaymentCommand(
            orderId = request.orderId,
            memberId = request.memberId,
            totalAmount = request.totalAmount,
            paymentDetails = request.paymentDetails.map { detail ->
                PaymentDetailCommand(
                    method = PaymentMethod.valueOf(detail.method),
                    amount = detail.amount,
                    metadata = detail.metadata
                )
            }
        )
        
        val payment = processPaymentUseCase.processPayment(command)
        return PaymentResponse.from(payment)
    }
    
    @PostMapping("/{paymentId}/cancel")
    fun cancelPayment(@PathVariable paymentId: Long): PaymentResponse {
        val payment = cancelPaymentUseCase.cancelPayment(paymentId)
        return PaymentResponse.from(payment)
    }
    
    @PostMapping("/{paymentId}/refund")
    fun refundPayment(
        @PathVariable paymentId: Long,
        @RequestBody request: RefundPaymentRequest
    ): PaymentResponse {
        val command = RefundPaymentCommand(
            paymentId = paymentId,
            refundDetails = request.refundDetails.map { detail ->
                RefundDetailCommand(
                    paymentDetailId = detail.paymentDetailId,
                    refundAmount = detail.refundAmount
                )
            }
        )
        
        val payment = refundPaymentUseCase.refundPayment(command)
        return PaymentResponse.from(payment)
    }
    
    @GetMapping("/{paymentId}")
    fun getPayment(@PathVariable paymentId: Long): PaymentResponse {
        val payment = getPaymentQuery.getPayment(paymentId)
            ?: throw NoSuchElementException("결제를 찾을 수 없습니다: $paymentId")
        return PaymentResponse.from(payment)
    }
    
    @GetMapping("/order/{orderId}")
    fun getPaymentByOrderId(@PathVariable orderId: Long): PaymentResponse {
        val payment = getPaymentQuery.getPaymentByOrderId(orderId)
            ?: throw NoSuchElementException("주문에 대한 결제를 찾을 수 없습니다: $orderId")
        return PaymentResponse.from(payment)
    }
    
    @GetMapping("/hello")
    fun hello(): String {
        return "Hello from Payment Service!"
    }
}

// Request DTOs
data class ProcessPaymentRequest(
    val orderId: Long,
    val memberId: Long,
    val totalAmount: Long,
    val paymentDetails: List<PaymentDetailRequest>
)

data class PaymentDetailRequest(
    val method: String,  // "PG_KPN", "POINT", "COUPON", "BNPL"
    val amount: Long,
    val metadata: Map<String, String> = emptyMap()
)

data class RefundPaymentRequest(
    val refundDetails: List<RefundDetailRequest>
)

data class RefundDetailRequest(
    val paymentDetailId: Long,
    val refundAmount: Long
)

// Response DTOs
data class PaymentResponse(
    val id: Long?,
    val orderId: Long,
    val totalAmount: Long,
    val status: String,
    val paymentDetails: List<PaymentDetailResponse>,
    val createdAt: String,
    val completedAt: String?
) {
    companion object {
        fun from(payment: Payment): PaymentResponse {
            return PaymentResponse(
                id = payment.id,
                orderId = payment.orderId,
                totalAmount = payment.totalAmount,
                status = payment.status.name,
                paymentDetails = payment.paymentDetails.map { PaymentDetailResponse.from(it) },
                createdAt = payment.createdAt.toString(),
                completedAt = payment.completedAt?.toString()
            )
        }
    }
}

data class PaymentDetailResponse(
    val id: Long?,
    val method: String,
    val amount: Long,
    val transactionId: String?,
    val status: String,
    val metadata: Map<String, String>
) {
    companion object {
        fun from(detail: com.vincenzo.shopping.payment.domain.PaymentDetail): PaymentDetailResponse {
            return PaymentDetailResponse(
                id = detail.id,
                method = detail.method.name,
                amount = detail.amount,
                transactionId = detail.transactionId,
                status = detail.status.name,
                metadata = detail.metadata
            )
        }
    }
}