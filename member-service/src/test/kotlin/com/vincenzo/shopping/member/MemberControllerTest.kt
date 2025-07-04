package com.vincenzo.shopping.member

import com.fasterxml.jackson.databind.ObjectMapper
import com.vincenzo.shopping.member.adapter.`in`.web.CreateMemberRequest
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
class MemberControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `회원 생성 성공`() {
        // given
        val request = CreateMemberRequest(
            email = "test@example.com",
            name = "테스트 유저",
            phoneNumber = "010-1234-5678"
        )

        // when & then
        mockMvc.perform(
            post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.email").value(request.email))
            .andExpect(jsonPath("$.name").value(request.name))
            .andExpect(jsonPath("$.phoneNumber").value(request.phoneNumber))
            .andExpect(jsonPath("$.point").value(0))
            .andExpect(jsonPath("$.id").exists())
    }

    @Test
    fun `hello 엔드포인트 테스트`() {
        mockMvc.perform(get("/api/members/hello"))
            .andExpect(status().isOk)
            .andExpect(content().string("Hello from Member Service!"))
    }
}