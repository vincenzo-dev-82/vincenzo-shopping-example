package com.vincenzo.shopping.payment.adapter.out.grpc

import com.vincenzo.shopping.grpc.member.GetMemberRequest
import com.vincenzo.shopping.grpc.member.MemberServiceGrpcKt
import com.vincenzo.shopping.grpc.member.UpdateMemberPointRequest
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
                phoneNumber = response.phoneNumber,
                point = response.point
            )
        } catch (e: Exception) {
            println("[Payment-gRPC] 회원 조회 실패: ${e.message}")
            null
        }
    }
    
    suspend fun updateMemberPoint(memberId: Long, pointChange: Int): MemberInfo? {
        return try {
            val request = UpdateMemberPointRequest.newBuilder()
                .setMemberId(memberId)
                .setPointChange(pointChange)
                .build()
            
            val response = memberServiceStub.updateMemberPoint(request)
            
            MemberInfo(
                id = response.id,
                email = response.email,
                name = response.name,
                phoneNumber = response.phoneNumber,
                point = response.point
            )
        } catch (e: Exception) {
            println("[Payment-gRPC] 포인트 업데이트 실패: ${e.message}")
            null
        }
    }
}

data class MemberInfo(
    val id: Long,
    val email: String,
    val name: String,
    val phoneNumber: String,
    val point: Int
)