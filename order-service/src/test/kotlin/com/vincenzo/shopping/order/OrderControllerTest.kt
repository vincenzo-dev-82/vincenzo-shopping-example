package com.vincenzo.shopping.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.vincenzo.shopping.common.kafka.KafkaMessage
import com.vincenzo.shopping.order.adapter.`in`.web.CreateOrderApiRequest
import com.vincenzo.shopping.order.adapter.`in`.web.OrderController
import com.vincenzo.shopping.order.adapter.`in`.web.OrderItemApiRequest
import com.vincenzo.shopping.order.adapter.out.grpc.MemberInfo
import com.vincenzo.shopping.order.adapter.out.grpc.MemberServiceGrpcClient
import com.vincenzo.shopping.order.adapter.out.grpc.ProductInfo
import com.vincenzo.shopping.order.adapter.out.grpc.ProductServiceGrpcClient
import com.vincenzo.shopping.order.application.service.OrderService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(OrderController::class)
class OrderControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var memberServiceGrpcClient: MemberServiceGrpcClient

    @MockBean
    private lateinit var productServiceGrpcClient: ProductServiceGrpcClient

    @MockBean
    private lateinit var orderService: OrderService

    @MockBean
    private lateinit var kafkaTemplate: KafkaTemplate<String, KafkaMessage<*>>

    @Test
    fun `hello 엔드포인트 테스트`() {
        mockMvc.perform(get("/api/orders/hello"))
            .andExpect(status().isOk)
            .andExpect(content().string("Hello from Order Service!"))
    }

    @Test
    fun `주문 생성 API 테스트`() {
        // given
        val request = CreateOrderApiRequest(
            memberId = 1L,
            items = listOf(
                OrderItemApiRequest(
                    productId = 1L,
                    quantity = 2
                )
            ),
            paymentMethod = "PG_KPN"
        )

        // OrderService가 정상적으로 주문을 생성한다고 가정
        whenever(orderService.createOrder(any())).thenReturn(
            com.vincenzo.shopping.order.domain.Order(
                id = 123L,
                memberId = 1L,
                orderItems = listOf(
                    com.vincenzo.shopping.order.domain.OrderItem(
                        productId = 1L,
                        productName = "테스트 상품",
                        price = 1000L,
                        quantity = 2
                    )
                ),
                totalAmount = 2000L,
                status = com.vincenzo.shopping.order.domain.OrderStatus.PENDING,
                paymentMethod = com.vincenzo.shopping.order.domain.PaymentMethod.PG_KPN
            )
        )

        // when & then
        mockMvc.perform(
            post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.orderId").value(123))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.totalAmount").value(2000))
    }

    @Test
    fun `회원 gRPC 테스트 엔드포인트`() {
        // given
        val memberId = 1L
        val memberInfo = MemberInfo(
            id = memberId,
            email = "test@example.com",
            name = "Test User",
            phoneNumber = "010-1234-5678",
            point = 1000
        )

        runBlocking {
            whenever(memberServiceGrpcClient.getMember(memberId)).thenReturn(memberInfo)
        }

        // when & then
        mockMvc.perform(get("/api/orders/test-grpc/member/$memberId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(memberId))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.name").value("Test User"))
    }

    @Test
    fun `상품 gRPC 테스트 엔드포인트`() {
        // given
        val productId = 1L
        val productInfo = ProductInfo(
            id = productId,
            name = "테스트 상품",
            price = 1000L,
            stock = 100,
            sellerId = "test-seller"
        )

        runBlocking {
            whenever(productServiceGrpcClient.getProduct(productId)).thenReturn(productInfo)
        }

        // when & then
        mockMvc.perform(get("/api/orders/test-grpc/product/$productId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(productId))
            .andExpect(jsonPath("$.name").value("테스트 상품"))
            .andExpect(jsonPath("$.price").value(1000))
    }
}
