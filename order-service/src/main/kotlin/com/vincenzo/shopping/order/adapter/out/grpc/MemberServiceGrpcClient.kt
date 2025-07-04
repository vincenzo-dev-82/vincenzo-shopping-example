package com.vincenzo.shopping.order.adapter.out.grpc

import com.vincenzo.shopping.grpc.member.GetMemberRequest
import com.vincenzo.shopping.grpc.member.MemberServiceGrpcKt
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component

@Component
class MemberServiceGrpcClient {
    
    @GrpcClient("member-service")
    private lateinit var memberServiceStub: MemberServiceGrpcKt.MemberServiceCoroutineStub
    
    suspend fun getMember(memberId: Long): MemberInfo? {
        return try {
            val request = GetMemberRequest.newBuilder()
                .setMemberId(memberId)
                .build()
            
            val response = memberServiceStub.getMember(request)
            
            MemberInfo(
                id = response.id,
                email = response.email,
                name = response.name,
                phoneNumber = response.phoneNumber
            )
        } catch (e: Exception) {
            null
        }
    }
}

data class MemberInfo(
    val id: Long,
    val email: String,
    val name: String,
    val phoneNumber: String
)