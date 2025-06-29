package com.vincenzo.shopping.member.application.port.`in`

import com.vincenzo.shopping.member.domain.Member

interface GetMemberQuery {
    fun getMember(memberId: Long): Member?
}
