package com.vincenzo.shopping.member.application.port.`in`

import com.vincenzo.shopping.member.domain.Member

interface UpdateMemberPointUseCase {
    fun updatePoint(memberId: Long, pointChange: Int): Member
}
