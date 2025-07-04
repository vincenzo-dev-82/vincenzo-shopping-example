package com.vincenzo.shopping.order

import com.vincenzo.shopping.order.adapter.out.grpc.MemberInfo
import com.vincenzo.shopping.order.adapter.out.grpc.MemberServiceGrpcClient
import com.vincenzo.shopping.order.adapter.out.grpc.ProductInfo
import com.vincenzo.shopping.order.adapter.out.grpc.ProductServiceGrpcClient
import com.vincenzo.shopping.order.application.service.CreateOrderRequest
import com.vincenzo.shopping.order.application.service.OrderItemRequest
import com.vincenzo.shopping.order.application.service.OrderService
import com.vincenzo.shopping.order.domain.PaymentMethod
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.kafka.core.KafkaTemplate

class OrderServiceTest {

    private val memberServiceGrpcClient: MemberServiceGrpcClient = mockk()
    private val productServiceGrpcClient: ProductServiceGrpcClient = mockk()
    private val kafkaTemplate: KafkaTemplate<String, com.vincenzo.shopping.common.kafka.KafkaMessage<*>> = mockk(relaxed = true)
    private val orderService = OrderService(memberServiceGrpcClient, productServiceGrpcClient, kafkaTemplate)

    @Test
    fun `주문 생성 성공`() = runBlocking {
        // given
        val request = CreateOrderRequest(
            memberId = 1L,
            items = listOf(
                OrderItemRequest(productId = 1L, quantity = 2)
            ),
            paymentMethod = PaymentMethod.PG_KPN
        )

        val member = MemberInfo(
            id = 1L,
            email = "test@example.com",
            name = "테스트 회원",
            phoneNumber = "010-1234-5678",
            point = 10000
        )

        val product = ProductInfo(
            id = 1L,
            name = "테스트 상품",
            price = 5000,
            stock = 10,
            sellerId = "seller001"
        )

        coEvery { memberServiceGrpcClient.getMember(1L) } returns member
        coEvery { productServiceGrpcClient.getProduct(1L) } returns product
        coEvery { productServiceGrpcClient.updateStock(1L, -2) } returns product.copy(stock = 8)

        // when
        val result = orderService.createOrder(request)

        // then
        assertNotNull(result)
        assertEquals(request.memberId, result.memberId)
        assertEquals(10000L, result.totalAmount)  // 5000 * 2
        assertEquals(1, result.orderItems.size)
    }

    @Test
    fun `주문 생성 실패 - 회원 없음`() = runBlocking {
        // given
        val request = CreateOrderRequest(
            memberId = 999L,
            items = listOf(
                OrderItemRequest(productId = 1L, quantity = 2)
            ),
            paymentMethod = PaymentMethod.PG_KPN
        )

        coEvery { memberServiceGrpcClient.getMember(999L) } returns null

        // when & then
        assertThrows<IllegalArgumentException> {
            runBlocking {
                orderService.createOrder(request)
            }
        }
    }

    @Test
    fun `주문 생성 실패 - 재고 부족`() = runBlocking {
        // given
        val request = CreateOrderRequest(
            memberId = 1L,
            items = listOf(
                OrderItemRequest(productId = 1L, quantity = 20)
            ),
            paymentMethod = PaymentMethod.PG_KPN
        )

        val member = MemberInfo(
            id = 1L,
            email = "test@example.com",
            name = "테스트 회원",
            phoneNumber = "010-1234-5678",
            point = 10000
        )

        val product = ProductInfo(
            id = 1L,
            name = "테스트 상품",
            price = 5000,
            stock = 10,  // 재고 10개
            sellerId = "seller001"
        )

        coEvery { memberServiceGrpcClient.getMember(1L) } returns member
        coEvery { productServiceGrpcClient.getProduct(1L) } returns product

        // when & then
        assertThrows<IllegalStateException> {
            runBlocking {
                orderService.createOrder(request)
            }
        }
    }
}