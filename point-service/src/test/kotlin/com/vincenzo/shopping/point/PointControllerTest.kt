package com.vincenzo.shopping.point

import com.fasterxml.jackson.databind.ObjectMapper
import com.vincenzo.shopping.point.adapter.`in`.web.ChargePointRequest
import com.vincenzo.shopping.point.adapter.`in`.web.UsePointRequest
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
class PointControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `포인트 충전 성공`() {
        // given
        val request = ChargePointRequest(
            memberId = 1L,
            amount = 10000,
            description = "테스트 충전"
        )

        // when & then
        mockMvc.perform(
            post("/api/points/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.memberId").value(request.memberId))
            .andExpect(jsonPath("$.amount").value(request.amount))
            .andExpect(jsonPath("$.type").value("CHARGE"))
            .andExpect(jsonPath("$.description").value(request.description))
    }

    @Test
    fun `포인트 사용 성공`() {
        // given - 먼저 충전
        val chargeRequest = ChargePointRequest(
            memberId = 2L,
            amount = 10000,
            description = "테스트 충전"
        )
        
        mockMvc.perform(
            post("/api/points/charge")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(chargeRequest))
        )
        
        // 포인트 사용
        val useRequest = UsePointRequest(
            memberId = 2L,
            amount = 5000,
            description = "테스트 사용",
            referenceId = "ORDER-001"
        )

        // when & then
        mockMvc.perform(
            post("/api/points/use")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(useRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.memberId").value(useRequest.memberId))
            .andExpect(jsonPath("$.amount").value(-useRequest.amount))
            .andExpect(jsonPath("$.type").value("USE"))
    }

    @Test
    fun `포인트 잔액 조회`() {
        // given
        val memberId = 1L

        // when & then
        mockMvc.perform(get("/api/points/balance/{memberId}", memberId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.memberId").value(memberId))
            .andExpect(jsonPath("$.balance").exists())
    }
}