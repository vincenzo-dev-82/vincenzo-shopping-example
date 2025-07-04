package com.vincenzo.shopping.member.adapter.`in`.grpc

import com.vincenzo.shopping.grpc.member.*
import com.vincenzo.shopping.member.application.port.`in`.GetMemberQuery
import com.vincenzo.shopping.member.domain.Member
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class MemberGrpcService(
    private val getMemberQuery: GetMemberQuery
) : MemberServiceGrpcKt.MemberServiceCoroutineImplBase() {
    
    override suspend fun getMember(request: GetMemberRequest): MemberResponse {
        val member = getMemberQuery.getMember(request.memberId)
            ?: throw IllegalArgumentException("Member not found: ${request.memberId}")
        
        return member.toGrpcResponse()
    }
}

private fun Member.toGrpcResponse(): MemberResponse {
    return MemberResponse.newBuilder()
        .setId(this.id ?: 0)
        .setEmail(this.email)
        .setName(this.name)
        .setPhoneNumber(this.phoneNumber)
        .build()
}