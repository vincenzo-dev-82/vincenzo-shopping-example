package com.vincenzo.shopping.member

import com.vincenzo.shopping.member.application.port.`in`.CreateMemberCommand
import com.vincenzo.shopping.member.application.port.out.MemberRepository
import com.vincenzo.shopping.member.application.service.MemberService
import com.vincenzo.shopping.member.domain.Member
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MemberServiceTest {

    private val memberRepository: MemberRepository = mockk()
    private val memberService = MemberService(memberRepository)

    @Test
    fun `회원 생성 성공`() {
        // given
        val command = CreateMemberCommand(
            email = "test@example.com",
            name = "테스트 유저",
            phoneNumber = "010-1234-5678"
        )
        
        val savedMember = Member(
            id = 1L,
            email = command.email,
            name = command.name,
            phoneNumber = command.phoneNumber,
            point = 0
        )
        
        every { memberRepository.save(any()) } returns savedMember

        // when
        val result = memberService.createMember(command)

        // then
        assertEquals(savedMember.email, result.email)
        assertEquals(savedMember.name, result.name)
        assertEquals(savedMember.phoneNumber, result.phoneNumber)
        assertEquals(0, result.point)
        
        verify(exactly = 1) { memberRepository.save(any()) }
    }

    @Test
    fun `회원 조회 성공`() {
        // given
        val memberId = 1L
        val member = Member(
            id = memberId,
            email = "test@example.com",
            name = "테스트 유저",
            phoneNumber = "010-1234-5678",
            point = 1000
        )
        
        every { memberRepository.findById(memberId) } returns member

        // when
        val result = memberService.getMember(memberId)

        // then
        assertNotNull(result)
        assertEquals(member.id, result?.id)
        assertEquals(member.email, result?.email)
        
        verify(exactly = 1) { memberRepository.findById(memberId) }
    }

    @Test
    fun `포인트 업데이트 성공`() {
        // given
        val memberId = 1L
        val pointChange = 500
        val member = Member(
            id = memberId,
            email = "test@example.com",
            name = "테스트 유저",
            phoneNumber = "010-1234-5678",
            point = 1000
        )
        
        val updatedMember = member.copy(point = member.point + pointChange)
        
        every { memberRepository.findById(memberId) } returns member
        every { memberRepository.save(any()) } returns updatedMember

        // when
        val result = memberService.updatePoint(memberId, pointChange)

        // then
        assertEquals(1500, result.point)
        
        verify(exactly = 1) { memberRepository.findById(memberId) }
        verify(exactly = 1) { memberRepository.save(any()) }
    }

    @Test
    fun `존재하지 않는 회원 포인트 업데이트 실패`() {
        // given
        val memberId = 999L
        every { memberRepository.findById(memberId) } returns null

        // when & then
        assertThrows<IllegalArgumentException> {
            memberService.updatePoint(memberId, 100)
        }
        
        verify(exactly = 1) { memberRepository.findById(memberId) }
        verify(exactly = 0) { memberRepository.save(any()) }
    }
}