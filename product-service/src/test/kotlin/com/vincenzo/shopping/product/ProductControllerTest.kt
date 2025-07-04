package com.vincenzo.shopping.product

import com.fasterxml.jackson.databind.ObjectMapper
import com.vincenzo.shopping.product.adapter.`in`.web.CreateProductRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `상품 생성 성공`() {
        // given
        val request = CreateProductRequest(
            name = "테스트 상품",
            price = 10000,
            stock = 100,
            sellerId = "seller001"
        )

        // when & then
        mockMvc.perform(
            post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.name").value(request.name))
            .andExpect(jsonPath("$.price").value(request.price))
            .andExpect(jsonPath("$.stock").value(request.stock))
            .andExpect(jsonPath("$.sellerId").value(request.sellerId))
            .andExpect(jsonPath("$.id").exists())
    }

    @Test
    fun `상품 조회 성공`() {
        // given - DataInitializer에서 생성한 상품 ID 사용
        val productId = 1L

        // when & then
        mockMvc.perform(get("/api/products/{productId}", productId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(productId))
            .andExpect(jsonPath("$.name").exists())
            .andExpect(jsonPath("$.price").exists())
            .andExpect(jsonPath("$.stock").exists())
    }

    @Test
    fun `모든 상품 조회`() {
        mockMvc.perform(get("/api/products"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
    }
}