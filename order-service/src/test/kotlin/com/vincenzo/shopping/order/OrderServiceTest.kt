package com.vincenzo.shopping.order

import com.vincenzo.shopping.common.kafka.KafkaMessage
import com.vincenzo.shopping.order.adapter.out.grpc.MemberInfo
import com.vincenzo.shopping.order.adapter.out.grpc.MemberServiceGrpcClient
import com.vincenzo.shopping.order.adapter.out.grpc.ProductInfo
import com.vincenzo.shopping.order.adapter.out.grpc.ProductServiceGrpcClient
import com.vincenzo.shopping.order.application.service.CreateOrderRequest
import com.vincenzo.shopping.order.application.service.OrderItemRequest
import com.vincenzo.shopping.order.application.service.OrderService
import com.vincenzo.shopping.order.domain.PaymentMethod
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.kafka.core.KafkaTemplate

class OrderServiceTest {
    
    private lateinit var memberServiceGrpcClient: MemberServiceGrpcClient
    private lateinit var productServiceGrpcClient: ProductServiceGrpcClient
    private lateinit var kafkaTemplate: KafkaTemplate<String, KafkaMessage<*>>
    private lateinit var orderService: OrderService
    
    @BeforeEach
    fun setUp() {
        memberServiceGrpcClient = mock(MemberServiceGrpcClient::class.java)
        productServiceGrpcClient = mock(ProductServiceGrpcClient::class.java)
        kafkaTemplate = mock(KafkaTemplate::class.java) as KafkaTemplate<String, KafkaMessage<*>>
        orderService = OrderService(memberServiceGrpcClient, productServiceGrpcClient, kafkaTemplate)
    }
    
    @Test
    fun `주문 생성 성공 - PG 결제`() = runBlocking {
        // given
        val memberId = 1L
        val productId = 1L
        val quantity = 2
        
        val memberInfo = MemberInfo(
            id = memberId,
            email = "test@example.com",
            name = "Test User",
            phoneNumber = "010-1234-5678",
            point = 1000
        )
        
        val productInfo = ProductInfo(
            id = productId,
            name = "캐시노트 포인트 충전권 1000원",
            price = 1000,
            stock = 100,
            sellerId = "cashnote"
        )
        
        val request = CreateOrderRequest(
            memberId = memberId,
            items = listOf(
                OrderItemRequest(
                    productId = productId,
                    quantity = quantity
                )
            ),
            paymentMethod = PaymentMethod.PG_KPN
        )
        
        whenever(memberServiceGrpcClient.getMember(memberId)).thenReturn(memberInfo)
        whenever(productServiceGrpcClient.getProduct(productId)).thenReturn(productInfo)
        whenever(productServiceGrpcClient.updateStock(productId, -quantity)).thenReturn(
            productInfo.copy(stock = productInfo.stock - quantity)
        )
        whenever(kafkaTemplate.send(any(), any<KafkaMessage<*>>())).thenReturn(null)
        
        // when
        val result = orderService.createOrder(request)
        
        // then
        assertNotNull(result)
        assertEquals(memberId, result.memberId)
        assertEquals(1, result.orderItems.size)
        assertEquals(productId, result.orderItems[0].productId)
        assertEquals(quantity, result.orderItems[0].quantity)
        assertEquals(2000L, result.totalAmount)
        assertEquals(PaymentMethod.PG_KPN, result.paymentMethod)
        
        verify(productServiceGrpcClient).updateStock(productId, -quantity)
        verify(kafkaTemplate).send(any(), any<KafkaMessage<*>>())
    }
    
    @Test
    fun `주문 생성 성공 - 캐시노트 포인트 결제`() = runBlocking {
        // given
        val memberId = 1L
        val productId = 1L
        val quantity = 1
        
        val memberInfo = MemberInfo(
            id = memberId,
            email = "test@example.com",
            name = "Test User",
            phoneNumber = "010-1234-5678",
            point = 2000
        )
        
        val productInfo = ProductInfo(
            id = productId,
            name = "캐시노트 포인트 충전권 1000원",
            price = 1000,
            stock = 100,
            sellerId = "cashnote"
        )
        
        val request = CreateOrderRequest(
            memberId = memberId,
            items = listOf(
                OrderItemRequest(
                    productId = productId,
                    quantity = quantity
                )
            ),
            paymentMethod = PaymentMethod.CASHNOTE_POINT
        )
        
        whenever(memberServiceGrpcClient.getMember(memberId)).thenReturn(memberInfo)
        whenever(productServiceGrpcClient.getProduct(productId)).thenReturn(productInfo)
        whenever(productServiceGrpcClient.updateStock(productId, -quantity)).thenReturn(
            productInfo.copy(stock = productInfo.stock - quantity)
        )
        whenever(kafkaTemplate.send(any(), any<KafkaMessage<*>>())).thenReturn(null)
        
        // when
        val result = orderService.createOrder(request)
        
        // then
        assertNotNull(result)
        assertEquals(PaymentMethod.CASHNOTE_POINT, result.paymentMethod)
    }
    
    @Test
    fun `주문 생성 실패 - 캐시노트 포인트 부족`() = runBlocking {
        // given
        val memberId = 1L
        val productId = 1L
        val quantity = 3
        
        val memberInfo = MemberInfo(
            id = memberId,
            email = "test@example.com",
            name = "Test User",
            phoneNumber = "010-1234-5678",
            point = 1000  // 포인트 부족
        )
        
        val productInfo = ProductInfo(
            id = productId,
            name = "캐시노트 포인트 충전권 1000원",
            price = 1000,
            stock = 100,
            sellerId = "cashnote"
        )
        
        val request = CreateOrderRequest(
            memberId = memberId,
            items = listOf(
                OrderItemRequest(
                    productId = productId,
                    quantity = quantity
                )
            ),
            paymentMethod = PaymentMethod.CASHNOTE_POINT
        )
        
        whenever(memberServiceGrpcClient.getMember(memberId)).thenReturn(memberInfo)
        whenever(productServiceGrpcClient.getProduct(productId)).thenReturn(productInfo)
        
        // when & then
        assertThrows<IllegalArgumentException> {
            orderService.createOrder(request)
        }
    }
    
    @Test
    fun `주문 생성 실패 - 재고 부족`() = runBlocking {
        // given
        val memberId = 1L
        val productId = 1L
        val quantity = 200  // 재고보다 많은 수량
        
        val memberInfo = MemberInfo(
            id = memberId,
            email = "test@example.com",
            name = "Test User",
            phoneNumber = "010-1234-5678",
            point = 1000000
        )
        
        val productInfo = ProductInfo(
            id = productId,
            name = "캐시노트 포인트 충전권 1000원",
            price = 1000,
            stock = 100,  // 재고 100개
            sellerId = "cashnote"
        )
        
        val request = CreateOrderRequest(
            memberId = memberId,
            items = listOf(
                OrderItemRequest(
                    productId = productId,
                    quantity = quantity
                )
            ),
            paymentMethod = PaymentMethod.PG_KPN
        )
        
        whenever(memberServiceGrpcClient.getMember(memberId)).thenReturn(memberInfo)
        whenever(productServiceGrpcClient.getProduct(productId)).thenReturn(productInfo)
        
        // when & then
        assertThrows<IllegalStateException> {
            orderService.createOrder(request)
        }
    }
    
    @Test
    fun `주문 생성 실패 - 회원이 존재하지 않음`() = runBlocking {
        // given
        val memberId = 999L
        val productId = 1L
        
        val request = CreateOrderRequest(
            memberId = memberId,
            items = listOf(
                OrderItemRequest(
                    productId = productId,
                    quantity = 1
                )
            ),
            paymentMethod = PaymentMethod.PG_KPN
        )
        
        whenever(memberServiceGrpcClient.getMember(memberId)).thenReturn(null)
        
        // when & then
        assertThrows<IllegalArgumentException> {
            orderService.createOrder(request)
        }
    }
    
    @Test
    fun `주문 생성 실패 - 상품이 존재하지 않음`() = runBlocking {
        // given
        val memberId = 1L
        val productId = 999L
        
        val memberInfo = MemberInfo(
            id = memberId,
            email = "test@example.com",
            name = "Test User",
            phoneNumber = "010-1234-5678",
            point = 1000
        )
        
        val request = CreateOrderRequest(
            memberId = memberId,
            items = listOf(
                OrderItemRequest(
                    productId = productId,
                    quantity = 1
                )
            ),
            paymentMethod = PaymentMethod.PG_KPN
        )
        
        whenever(memberServiceGrpcClient.getMember(memberId)).thenReturn(memberInfo)
        whenever(productServiceGrpcClient.getProduct(productId)).thenReturn(null)
        
        // when & then
        assertThrows<IllegalArgumentException> {
            orderService.createOrder(request)
        }
    }
}
