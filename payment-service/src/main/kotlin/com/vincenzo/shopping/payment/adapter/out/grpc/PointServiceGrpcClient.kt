package com.vincenzo.shopping.payment.adapter.out.grpc

import com.vincenzo.shopping.grpc.point.*
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component

@Component
class PointServiceGrpcClient {
    
    @GrpcClient("point-service")
    private lateinit var pointServiceStub: PointServiceGrpcKt.PointServiceCoroutineStub
    
    suspend fun getBalance(memberId: Long): Int? {
        return try {
            val request = GetBalanceRequest.newBuilder()
                .setMemberId(memberId)
                .build()
            
            val response = pointServiceStub.getBalance(request)
            response.balance
        } catch (e: Exception) {
            println("포인트 잔액 조회 실패: ${e.message}")
            null
        }
    }
    
    suspend fun usePoint(memberId: Long, amount: Int, description: String, referenceId: String): Boolean {
        return try {
            val request = UsePointRequest.newBuilder()
                .setMemberId(memberId)
                .setAmount(amount)
                .setDescription(description)
                .setReferenceId(referenceId)
                .build()
            
            pointServiceStub.usePoint(request)
            true
        } catch (e: Exception) {
            println("포인트 사용 실패: ${e.message}")
            false
        }
    }
    
    suspend fun refundPoint(memberId: Long, amount: Int, description: String, referenceId: String): Boolean {
        return try {
            val request = RefundPointRequest.newBuilder()
                .setMemberId(memberId)
                .setAmount(amount)
                .setDescription(description)
                .setReferenceId(referenceId)
                .build()
            
            pointServiceStub.refundPoint(request)
            true
        } catch (e: Exception) {
            println("포인트 환불 실패: ${e.message}")
            false
        }
    }
}