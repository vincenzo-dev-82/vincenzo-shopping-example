package com.vincenzo.shopping.order.adapter.`in`.web

import com.vincenzo.shopping.order.adapter.out.grpc.MemberServiceGrpcClient
import com.vincenzo.shopping.order.adapter.out.grpc.ProductServiceGrpcClient
import com.vincenzo.shopping.order.application.service.CreateOrderRequest
import com.vincenzo.shopping.order.application.service.OrderItemRequest
import com.vincenzo.shopping.order.application.service.OrderService
import com.vincenzo.shopping.order.domain.OrderPaymentDetail
import com.vincenzo.shopping.order.domain.PaymentMethod
import kotlinx.coroutines.runBlocking
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val memberServiceGrpcClient: MemberServiceGrpcClient,
    private val productServiceGrpcClient: ProductServiceGrpcClient,
    private val orderService: OrderService
) {
    
    @GetMapping("/hello")
    fun hello(): String {
        return "Hello from Order Service!"
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createOrder(@RequestBody request: CreateOrderApiRequest): Any {
        return try {
            // 결제 방법 결정
            val paymentMethod = determinePaymentMethod(request)
            
            // 결제 상세 정보 생성
            val paymentDetails = createPaymentDetails(request, paymentMethod)
            
            val createOrderRequest = CreateOrderRequest(
                memberId = request.memberId,
                items = request.items.map { 
                    OrderItemRequest(
                        productId = it.productId,
                        quantity = it.quantity
                    )
                },
                paymentMethod = paymentMethod,
                paymentDetails = paymentDetails
            )
            
            val order = orderService.createOrder(createOrderRequest)
            
            mapOf(
                "orderId" to order.id,
                "status" to order.status,
                "totalAmount" to order.totalAmount,
                "paymentMethod" to order.paymentMethod,
                "paymentDetails" to order.paymentDetails.map {
                    mapOf(
                        "method" to it.method,
                        "amount" to it.amount,
                        "metadata" to it.metadata
                    )
                },
                "items" to order.orderItems
            )
        } catch (e: Exception) {
            mapOf(
                "error" to e.message,
                "status" to "FAILED"
            )
        }
    }
    
    private fun determinePaymentMethod(request: CreateOrderApiRequest): PaymentMethod {
        // 단일 결제 수단
        if (request.paymentMethod != null) {
            return PaymentMethod.valueOf(request.paymentMethod)
        }
        
        // 복합 결제
        if (request.compositePayment != null && request.compositePayment.details.size > 1) {
            // 쿠폰 단독 결제 불가 검증
            if (request.compositePayment.details.size == 1 && 
                request.compositePayment.details[0].method == "COUPON") {
                throw IllegalArgumentException("쿠폰은 단독으로 결제할 수 없습니다.")
            }
            
            // BNPL은 복합결제 불가 검증
            if (request.compositePayment.details.any { it.method == "BNPL" }) {
                throw IllegalArgumentException("BNPL은 다른 결제 수단과 함께 사용할 수 없습니다.")
            }
            
            // 복합결제는 PG가 필수
            if (!request.compositePayment.details.any { it.method == "PG_KPN" }) {
                throw IllegalArgumentException("복합결제 시 PG 결제가 포함되어야 합니다.")
            }
            
            // 포인트가 포함된 경우 포인트 금액이 전체인지 확인
            val pointDetail = request.compositePayment.details.find { it.method == "CASHNOTE_POINT" }
            if (pointDetail != null) {
                val totalAmount = request.compositePayment.details.sumOf { it.amount }
                if (pointDetail.amount == totalAmount) {
                    return PaymentMethod.CASHNOTE_POINT
                }
            }
            
            return PaymentMethod.valueOf("COMPOSITE")
        }
        
        throw IllegalArgumentException("결제 방법을 지정해야 합니다.")
    }
    
    private fun createPaymentDetails(request: CreateOrderApiRequest, paymentMethod: PaymentMethod): List<OrderPaymentDetail>? {
        if (request.compositePayment != null) {
            return request.compositePayment.details.map { detail ->
                OrderPaymentDetail(
                    method = PaymentMethod.valueOf(detail.method),
                    amount = detail.amount,
                    metadata = detail.metadata ?: emptyMap()
                )
            }
        }
        return null
    }
    
    @GetMapping("/test-grpc/member/{memberId}")
    fun testMemberGrpc(@PathVariable memberId: Long): Any {
        return runBlocking {
            memberServiceGrpcClient.getMember(memberId)
                ?: mapOf("error" to "Member not found")
        }
    }
    
    @GetMapping("/test-grpc/product/{productId}")
    fun testProductGrpc(@PathVariable productId: Long): Any {
        return runBlocking {
            productServiceGrpcClient.getProduct(productId)
                ?: mapOf("error" to "Product not found")
        }
    }
}

data class CreateOrderApiRequest(
    val memberId: Long,
    val items: List<OrderItemApiRequest>,
    val paymentMethod: String? = null,  // 단일 결제: "PG_KPN", "CASHNOTE_POINT", "BNPL"
    val compositePayment: CompositePaymentRequest? = null  // 복합 결제
)

data class OrderItemApiRequest(
    val productId: Long,
    val quantity: Int
)

data class CompositePaymentRequest(
    val details: List<PaymentDetailRequest>
)

data class PaymentDetailRequest(
    val method: String,  // "PG_KPN", "CASHNOTE_POINT", "COUPON"
    val amount: Long,
    val metadata: Map<String, String>? = null  // 쿠폰 코드 등
)
