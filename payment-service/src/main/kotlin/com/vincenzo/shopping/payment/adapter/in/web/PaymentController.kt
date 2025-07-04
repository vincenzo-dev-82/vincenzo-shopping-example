package com.vincenzo.shopping.payment.adapter.`in`.web

import com.vincenzo.shopping.payment.application.port.`in`.PaymentDetailCommand
import com.vincenzo.shopping.payment.application.port.`in`.ProcessPaymentCommand
import com.vincenzo.shopping.payment.application.port.`in`.ProcessPaymentUseCase
import com.vincenzo.shopping.payment.domain.Payment
import com.vincenzo.shopping.payment.domain.PaymentMethod
import com.vincenzo.shopping.payment.domain.PaymentStatus
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/payments")
class PaymentController(
    private val processPaymentUseCase: ProcessPaymentUseCase
) {
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun processPayment(@RequestBody request: PaymentRequest): PaymentResponse = runBlocking {
        val paymentDetails = if (request.paymentMethod == "COMPOSITE" && request.compositeDetails != null) {
            // 복합결제
            val mainDetail = PaymentDetailCommand(
                method = PaymentMethod.valueOf(request.compositeDetails.mainMethod),
                amount = request.compositeDetails.mainAmount,
                metadata = request.compositeDetails.mainMetadata.mapValues { it.value.toString() }
            )
            
            val subDetails = request.compositeDetails.subPayments.map { sub ->
                PaymentDetailCommand(
                    method = PaymentMethod.valueOf(sub.method),
                    amount = sub.amount,
                    metadata = sub.metadata.mapValues { it.value.toString() }
                )
            }
            
            listOf(mainDetail) + subDetails
        } else {
            // 단일 결제
            listOf(
                PaymentDetailCommand(
                    method = PaymentMethod.valueOf(request.paymentMethod),
                    amount = request.totalAmount,
                    metadata = request.metadata.mapValues { it.value.toString() }
                )
            )
        }
        
        val command = ProcessPaymentCommand(
            orderId = request.orderId,
            memberId = request.memberId,
            totalAmount = request.totalAmount,
            paymentDetails = paymentDetails
        )
        
        val payment = processPaymentUseCase.processPayment(command)
        PaymentResponse.from(payment)
    }
    
    @GetMapping("/hello")
    fun hello(): String {
        return "Hello from Payment Service!"
    }
    
    @PostMapping("/test")
    @ResponseStatus(HttpStatus.CREATED)
    fun testPayment(@RequestBody request: TestPaymentRequest): Map<String, Any> = runBlocking {
        println("테스트 결제 요청: $request")
        
        // 간단한 테스트용 결제 처리
        val command = ProcessPaymentCommand(
            orderId = request.orderId,
            memberId = request.memberId,
            totalAmount = request.amount,
            paymentDetails = listOf(
                PaymentDetailCommand(
                    method = PaymentMethod.valueOf(request.method),
                    amount = request.amount,
                    metadata = when (request.method) {
                        "COUPON" -> mapOf("couponCode" to "WELCOME10")
                        "BNPL" -> mapOf("installmentMonths" to "3")
                        else -> emptyMap()
                    }
                )
            )
        )
        
        try {
            val payment = processPaymentUseCase.processPayment(command)
            mapOf<String, Any>(
                "success" to (payment.status == PaymentStatus.COMPLETED),
                "paymentId" to (payment.id ?: 0L),
                "status" to payment.status.toString(),
                "details" to payment.paymentDetails
            )
        } catch (e: Exception) {
            mapOf<String, Any>(
                "success" to false,
                "error" to (e.message ?: "Unknown error")
            )
        }
    }
}

data class PaymentRequest(
    val orderId: Long,
    val memberId: Long,
    val totalAmount: Long,
    val paymentMethod: String,
    val metadata: Map<String, Any> = emptyMap(),
    val compositeDetails: CompositePaymentRequest? = null,
    val installmentMonths: Int? = null,
    val couponCode: String? = null
)

data class CompositePaymentRequest(
    val mainMethod: String,
    val mainAmount: Long,
    val mainMetadata: Map<String, Any> = emptyMap(),
    val subPayments: List<SubPaymentRequest> = emptyList()
)

data class SubPaymentRequest(
    val method: String,
    val amount: Long,
    val metadata: Map<String, Any> = emptyMap()
)

data class TestPaymentRequest(
    val orderId: Long,
    val memberId: Long,
    val amount: Long,
    val method: String
)

data class PaymentResponse(
    val paymentId: Long?,
    val orderId: Long,
    val memberId: Long,
    val totalAmount: Long,
    val paymentMethod: String,
    val status: String,
    val details: List<PaymentDetailResponse>,
    val message: String? = null
) {
    companion object {
        fun from(payment: Payment): PaymentResponse {
            return PaymentResponse(
                paymentId = payment.id,
                orderId = payment.orderId,
                memberId = payment.memberId,
                totalAmount = payment.totalAmount,
                paymentMethod = payment.paymentMethod.name,
                status = payment.status.name,
                details = payment.paymentDetails.map { detail ->
                    PaymentDetailResponse(
                        method = detail.method.name,
                        amount = detail.amount,
                        transactionId = detail.transactionId
                    )
                },
                message = when (payment.status) {
                    PaymentStatus.FAILED -> 
                        payment.paymentDetails.firstOrNull()?.metadata?.get("failureReason") as? String
                    else -> null
                }
            )
        }
    }
}

data class PaymentDetailResponse(
    val method: String,
    val amount: Long,
    val transactionId: String?
)