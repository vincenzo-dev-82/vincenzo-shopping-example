package com.vincenzo.shopping.member.application.port.`in`

import com.vincenzo.shopping.member.domain.Member

interface CreateMemberUseCase {
    fun createMember(command: CreateMemberCommand): Member
}

data class CreateMemberCommand(
    val email: String,
    val name: String,
    val phoneNumber: String
)
