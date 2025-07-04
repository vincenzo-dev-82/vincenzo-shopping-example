package com.vincenzo.shopping.payment.adapter.`in`.web

import com.vincenzo.shopping.payment.application.port.`in`.GetPaymentQuery
import com.vincenzo.shopping.payment.application.port.`in`.PaymentDetailCommand
import com.vincenzo.shopping.payment.application.port.`in`.ProcessPaymentCommand
import com.vincenzo.shopping.payment.application.port.`in`.ProcessPaymentUseCase
import com.vincenzo.shopping.payment.domain.Payment
import com.vincenzo.shopping.payment.domain.PaymentMethod
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/payments")
class PaymentController(
    private val processPaymentUseCase: ProcessPaymentUseCase,
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
                    method = detail.method,
                    amount = detail.amount,
                    metadata = detail.metadata.mapValues { it.value.toString() }
                )
            }
        )
        
        return try {
            val payment = processPaymentUseCase.processPayment(command)
            PaymentResponse.from(payment)
        } catch (e: Exception) {
            throw PaymentException(e.message ?: "결제 처리 실패", e)
        }
    }
    
    @GetMapping("/{paymentId}")
    fun getPayment(@PathVariable paymentId: Long): PaymentResponse {
        val payment = getPaymentQuery.getPayment(paymentId)
            ?: throw PaymentNotFoundException("결제 정보를 찾을 수 없습니다: $paymentId")
        
        return PaymentResponse.from(payment)
    }
    
    @GetMapping("/order/{orderId}")
    fun getPaymentByOrderId(@PathVariable orderId: Long): PaymentResponse {
        val payment = getPaymentQuery.getPaymentByOrderId(orderId)
            ?: throw PaymentNotFoundException("주문에 대한 결제 정보를 찾을 수 없습니다: $orderId")
        
        return PaymentResponse.from(payment)
    }
    
    @PostMapping("/{paymentId}/cancel")
    fun cancelPayment(
        @PathVariable paymentId: Long,
        @RequestBody request: CancelPaymentRequest
    ): PaymentResponse {
        // 결제 취소 로직 (추후 구현)
        throw NotImplementedError("결제 취소 기능은 아직 구현되지 않았습니다")
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
    val method: PaymentMethod,
    val amount: Long,
    val metadata: Map<String, Any> = emptyMap()
)

data class CancelPaymentRequest(
    val reason: String
)

// Response DTOs
data class PaymentResponse(
    val paymentId: Long,
    val orderId: Long,
    val memberId: Long,
    val totalAmount: Long,
    val paymentMethod: String,
    val status: String,
    val paymentDetails: List<PaymentDetailResponse>,
    val createdAt: String,
    val completedAt: String?
) {
    companion object {
        fun from(payment: Payment): PaymentResponse {
            return PaymentResponse(
                paymentId = payment.id!!,
                orderId = payment.orderId,
                memberId = payment.memberId,
                totalAmount = payment.totalAmount,
                paymentMethod = payment.paymentMethod.name,
                status = payment.status.name,
                paymentDetails = payment.paymentDetails.map { detail ->
                    PaymentDetailResponse(
                        method = detail.method.name,
                        amount = detail.amount,
                        status = detail.status.name,
                        transactionId = detail.transactionId
                    )
                },
                createdAt = payment.createdAt.toString(),
                completedAt = payment.completedAt?.toString()
            )
        }
    }
}

data class PaymentDetailResponse(
    val method: String,
    val amount: Long,
    val status: String,
    val transactionId: String?
)

// Exceptions
@ResponseStatus(HttpStatus.NOT_FOUND)
class PaymentNotFoundException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.BAD_REQUEST)
class PaymentException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)