package com.vincenzo.shopping.payment.adapter.`in`.web

import com.vincenzo.shopping.payment.application.port.`in`.ProcessPaymentCommand
import com.vincenzo.shopping.payment.application.port.`in`.ProcessPaymentUseCase
import com.vincenzo.shopping.payment.application.processor.CompositePaymentPlan
import com.vincenzo.shopping.payment.application.processor.SubPayment
import com.vincenzo.shopping.payment.domain.Payment
import com.vincenzo.shopping.payment.domain.PaymentMethod
import com.vincenzo.shopping.payment.domain.PaymentStatus
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/payments")
class PaymentController(
    private val processPaymentUseCase: ProcessPaymentUseCase
) {
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun processPayment(@RequestBody request: PaymentRequest): PaymentResponse = runBlocking {
        val paymentDetails = when (request.paymentMethod) {
            "COMPOSITE" -> buildCompositePaymentDetails(request)
            "BNPL" -> mapOf("installmentMonths" to (request.installmentMonths ?: 1))
            "COUPON" -> mapOf("couponCode" to (request.couponCode ?: ""))
            else -> request.metadata
        }
        
        val command = ProcessPaymentCommand(
            orderId = request.orderId,
            memberId = request.memberId,
            totalAmount = request.totalAmount,
            paymentMethod = request.paymentMethod,
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
    fun testPayment(@RequestBody request: TestPaymentRequest): Any = runBlocking {
        println("테스트 결제 요청: $request")
        
        // 간단한 테스트용 결제 처리
        val command = ProcessPaymentCommand(
            orderId = request.orderId,
            memberId = request.memberId,
            totalAmount = request.amount,
            paymentMethod = request.method,
            paymentDetails = when (request.method) {
                "COUPON" -> mapOf("couponCode" to "WELCOME10")
                "BNPL" -> mapOf("installmentMonths" to 3)
                else -> emptyMap()
            }
        )
        
        try {
            val payment = processPaymentUseCase.processPayment(command)
            mapOf(
                "success" to (payment.status == PaymentStatus.COMPLETED),
                "paymentId" to payment.id,
                "status" to payment.status,
                "details" to payment.details
            )
        } catch (e: Exception) {
            mapOf(
                "success" to false,
                "error" to e.message
            )
        }
    }
    
    private fun buildCompositePaymentDetails(request: PaymentRequest): Map<String, Any> {
        val compositeDetails = request.compositeDetails
            ?: throw IllegalArgumentException("복합결제는 상세 정보가 필요합니다.")
        
        val subPayments = compositeDetails.subPayments.map { sub ->
            SubPayment(
                method = PaymentMethod.valueOf(sub.method),
                amount = sub.amount,
                metadata = sub.metadata
            )
        }
        
        return mapOf(
            "paymentPlan" to CompositePaymentPlan(
                mainMethod = PaymentMethod.valueOf(compositeDetails.mainMethod),
                mainAmount = compositeDetails.mainAmount,
                mainMetadata = compositeDetails.mainMetadata,
                subPayments = subPayments
            )
        )
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
                details = payment.details.map { detail ->
                    PaymentDetailResponse(
                        method = detail.method.name,
                        amount = detail.amount,
                        transactionId = detail.transactionId
                    )
                },
                message = when (payment.status) {
                    PaymentStatus.FAILED -> 
                        payment.details.firstOrNull()?.metadata?.get("failureReason") as? String
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