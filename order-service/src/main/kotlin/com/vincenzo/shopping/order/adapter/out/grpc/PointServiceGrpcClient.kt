package com.vincenzo.shopping.order.adapter.out.grpc

import com.vincenzo.shopping.grpc.point.*
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component

@Component
class PointServiceGrpcClient {
    
    @GrpcClient("point-service")
    private lateinit var pointServiceStub: PointServiceGrpcKt.PointServiceCoroutineStub
    
    suspend fun getBalance(memberId: Long): PointBalanceInfo? {
        return try {
            val request = GetBalanceRequest.newBuilder()
                .setMemberId(memberId)
                .build()
            
            val response = pointServiceStub.getBalance(request)
            
            PointBalanceInfo(
                memberId = response.memberId,
                balance = response.balance,
                updatedAt = response.updatedAt
            )
        } catch (e: Exception) {
            println("포인트 잔액 조회 실패: ${e.message}")
            null
        }
    }
}

data class PointBalanceInfo(
    val memberId: Long,
    val balance: Int,
    val updatedAt: String
)