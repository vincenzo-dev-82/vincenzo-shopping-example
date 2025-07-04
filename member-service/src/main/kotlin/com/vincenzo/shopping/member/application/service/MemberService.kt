package com.vincenzo.shopping.member.application.service

import com.vincenzo.shopping.member.application.port.`in`.CreateMemberCommand
import com.vincenzo.shopping.member.application.port.`in`.CreateMemberUseCase
import com.vincenzo.shopping.member.application.port.`in`.GetMemberQuery
import com.vincenzo.shopping.member.application.port.`in`.UpdateMemberPointUseCase
import com.vincenzo.shopping.member.application.port.out.MemberRepository
import com.vincenzo.shopping.member.domain.Member
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MemberService(
    private val memberRepository: MemberRepository
) : CreateMemberUseCase, GetMemberQuery, UpdateMemberPointUseCase {
    
    override fun createMember(command: CreateMemberCommand): Member {
        val member = Member(
            email = command.email,
            name = command.name,
            phoneNumber = command.phoneNumber
        )
        return memberRepository.save(member)
    }
    
    @Transactional(readOnly = true)
    override fun getMember(memberId: Long): Member? {
        return memberRepository.findById(memberId)
    }
    
    override fun updatePoint(memberId: Long, pointChange: Int): Member {
        val member = memberRepository.findById(memberId)
            ?: throw IllegalArgumentException("Member not found: $memberId")
        
        val updatedMember = member.copy(point = member.point + pointChange)
        return memberRepository.save(updatedMember)
    }
}
