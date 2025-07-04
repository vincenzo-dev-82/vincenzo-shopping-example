package com.vincenzo.shopping.payment

import com.fasterxml.jackson.databind.ObjectMapper
import com.vincenzo.shopping.payment.adapter.`in`.web.PaymentDetailRequest
import com.vincenzo.shopping.payment.adapter.`in`.web.ProcessPaymentRequest
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
class PaymentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `hello 엔드포인트 테스트`() {
        mockMvc.perform(get("/api/payments/hello"))
            .andExpect(status().isOk)
            .andExpect(content().string("Hello from Payment Service!"))
    }

    @Test
    fun `결제 처리 API 테스트`() {
        // given
        val request = ProcessPaymentRequest(
            orderId = 1L,
            memberId = 1L,
            totalAmount = 10000,
            paymentDetails = listOf(
                PaymentDetailRequest(
                    method = "PG_KPN",
                    amount = 10000,
                    metadata = emptyMap()
                )
            )
        )

        // when & then
        mockMvc.perform(
            post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.orderId").value(request.orderId))
            .andExpect(jsonPath("$.totalAmount").value(request.totalAmount))
            .andExpect(jsonPath("$.status").exists())
    }
}