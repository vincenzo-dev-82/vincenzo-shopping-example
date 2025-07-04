package com.vincenzo.shopping.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.vincenzo.shopping.order.adapter.`in`.web.CreateOrderApiRequest
import com.vincenzo.shopping.order.adapter.`in`.web.OrderItemApiRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

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

        // when & then
        mockMvc.perform(
            post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.orderId").exists())
            .andExpect(jsonPath("$.status").exists())
            .andExpect(jsonPath("$.totalAmount").exists())
    }
}