package com.vincenzo.shopping.order.adapter.`in`.web

import com.vincenzo.shopping.order.adapter.out.grpc.MemberServiceGrpcClient
import com.vincenzo.shopping.order.adapter.out.grpc.ProductServiceGrpcClient
import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val memberServiceGrpcClient: MemberServiceGrpcClient,
    private val productServiceGrpcClient: ProductServiceGrpcClient
) {
    
    @GetMapping("/hello")
    fun hello(): String {
        return "Hello from Order Service!"
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
