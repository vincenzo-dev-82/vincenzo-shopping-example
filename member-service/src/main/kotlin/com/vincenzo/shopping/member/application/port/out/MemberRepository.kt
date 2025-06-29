package com.vincenzo.shopping.member.application.port.out

import com.vincenzo.shopping.member.domain.Member

interface MemberRepository {
    fun save(member: Member): Member
    fun findById(id: Long): Member?
    fun findByEmail(email: String): Member?
}
