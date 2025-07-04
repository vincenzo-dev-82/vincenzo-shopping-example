package com.vincenzo.shopping.order.adapter.`in`.web

import com.vincenzo.shopping.order.adapter.out.grpc.MemberServiceGrpcClient
import com.vincenzo.shopping.order.adapter.out.grpc.ProductServiceGrpcClient
import com.vincenzo.shopping.order.application.service.CreateOrderRequest
import com.vincenzo.shopping.order.application.service.OrderItemRequest
import com.vincenzo.shopping.order.application.service.OrderService
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
            val createOrderRequest = CreateOrderRequest(
                memberId = request.memberId,
                items = request.items.map { 
                    OrderItemRequest(
                        productId = it.productId,
                        quantity = it.quantity
                    )
                },
                paymentMethod = PaymentMethod.valueOf(request.paymentMethod)
            )
            
            val order = orderService.createOrder(createOrderRequest)
            
            mapOf(
                "orderId" to order.id,
                "status" to order.status,
                "totalAmount" to order.totalAmount,
                "items" to order.orderItems
            )
        } catch (e: Exception) {
            mapOf(
                "error" to e.message,
                "status" to "FAILED"
            )
        }
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
    val paymentMethod: String  // "PG_KPN", "CASHNOTE_POINT", "BNPL", "COMPOSITE"
)

data class OrderItemApiRequest(
    val productId: Long,
    val quantity: Int
)
