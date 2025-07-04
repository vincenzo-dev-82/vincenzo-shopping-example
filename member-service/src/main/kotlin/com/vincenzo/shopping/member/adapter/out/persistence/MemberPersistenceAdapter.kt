package com.vincenzo.shopping.member.adapter.out.persistence

import com.vincenzo.shopping.member.application.port.out.MemberRepository
import com.vincenzo.shopping.member.domain.Member
import org.springframework.stereotype.Repository

@Repository
class MemberPersistenceAdapter(
    private val memberJpaRepository: MemberJpaRepository
) : MemberRepository {
    
    override fun save(member: Member): Member {
        val entity = MemberEntity(
            id = member.id,
            email = member.email,
            name = member.name,
            phoneNumber = member.phoneNumber
        )
        val savedEntity = memberJpaRepository.save(entity)
        return savedEntity.toDomain()
    }
    
    override fun findById(id: Long): Member? {
        return memberJpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }
    
    override fun findByEmail(email: String): Member? {
        return memberJpaRepository.findByEmail(email)?.toDomain()
    }
}

private fun MemberEntity.toDomain(): Member {
    return Member(
        id = this.id,
        email = this.email,
        name = this.name,
        phoneNumber = this.phoneNumber
    )
}