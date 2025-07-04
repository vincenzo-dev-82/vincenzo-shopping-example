package com.vincenzo.shopping.member

import com.vincenzo.shopping.member.application.port.`in`.CreateMemberCommand
import com.vincenzo.shopping.member.application.port.out.MemberRepository
import com.vincenzo.shopping.member.application.service.MemberService
import com.vincenzo.shopping.member.domain.Member
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class MemberServiceTest {
    
    private lateinit var memberRepository: MemberRepository
    private lateinit var memberService: MemberService
    
    @BeforeEach
    fun setUp() {
        memberRepository = mock(MemberRepository::class.java)
        memberService = MemberService(memberRepository)
    }
    
    @Test
    fun `회원 생성 성공`() {
        // given
        val command = CreateMemberCommand(
            email = "test@example.com",
            name = "Test User",
            phoneNumber = "010-1234-5678"
        )
        
        val savedMember = Member(
            id = 1L,
            email = command.email,
            name = command.name,
            phoneNumber = command.phoneNumber,
            point = 0
        )
        
        whenever(memberRepository.save(any())).thenReturn(savedMember)
        
        // when
        val result = memberService.createMember(command)
        
        // then
        assertEquals(savedMember.id, result.id)
        assertEquals(savedMember.email, result.email)
        assertEquals(savedMember.name, result.name)
        assertEquals(savedMember.phoneNumber, result.phoneNumber)
        assertEquals(0, result.point)
    }
    
    @Test
    fun `회원 조회 성공`() {
        // given
        val memberId = 1L
        val member = Member(
            id = memberId,
            email = "test@example.com",
            name = "Test User",
            phoneNumber = "010-1234-5678",
            point = 1000
        )
        
        whenever(memberRepository.findById(memberId)).thenReturn(member)
        
        // when
        val result = memberService.getMember(memberId)
        
        // then
        assertNotNull(result)
        assertEquals(member.id, result?.id)
        assertEquals(member.email, result?.email)
        assertEquals(member.name, result?.name)
        assertEquals(member.phoneNumber, result?.phoneNumber)
        assertEquals(member.point, result?.point)
    }
    
    @Test
    fun `회원 조회 실패 - 회원이 존재하지 않음`() {
        // given
        val memberId = 999L
        whenever(memberRepository.findById(memberId)).thenReturn(null)
        
        // when
        val result = memberService.getMember(memberId)
        
        // then
        assertNull(result)
    }
    
    @Test
    fun `포인트 업데이트 성공`() {
        // given
        val memberId = 1L
        val pointChange = 500
        val member = Member(
            id = memberId,
            email = "test@example.com",
            name = "Test User",
            phoneNumber = "010-1234-5678",
            point = 1000
        )
        
        val updatedMember = member.copy(point = member.point + pointChange)
        
        whenever(memberRepository.findById(memberId)).thenReturn(member)
        whenever(memberRepository.save(any())).thenReturn(updatedMember)
        
        // when
        val result = memberService.updatePoint(memberId, pointChange)
        
        // then
        assertEquals(1500, result.point)
    }
    
    @Test
    fun `포인트 업데이트 실패 - 회원이 존재하지 않음`() {
        // given
        val memberId = 999L
        val pointChange = 500
        
        whenever(memberRepository.findById(memberId)).thenReturn(null)
        
        // when & then
        assertThrows<IllegalArgumentException> {
            memberService.updatePoint(memberId, pointChange)
        }
    }
}
