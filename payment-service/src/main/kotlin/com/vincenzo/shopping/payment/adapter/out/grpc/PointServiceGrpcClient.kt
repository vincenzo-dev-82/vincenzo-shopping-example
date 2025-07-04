package com.vincenzo.shopping.payment.adapter.out.grpc

import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component
import kotlin.random.Random

/**
 * 캐시노트 포인트 서비스 gRPC 클라이언트 (Mock)
 * 
 * 실제 환경에서는 별도의 포인트 서비스와 통신
 * 현재는 Mock 구현으로 시뮬레이션
 */
@Component
class PointServiceGrpcClient {
    
    // 테스트를 위한 회원별 포인트 잔액 (메모리 저장)
    private val memberPoints = mutableMapOf<Long, Long>(
        1L to 100000L,
        2L to 50000L,
        3L to 20000L
    )
    
    /**
     * 포인트 잔액 조회
     */
    suspend fun getBalance(memberId: Long): Long? {
        println("[PointService] 포인트 잔액 조회 - 회원: $memberId")
        
        // 실패 시뮬레이션 (5% 확률)
        if (Random.nextDouble() < 0.05) {
            println("[PointService] 잔액 조회 실패")
            return null
        }
        
        val balance = memberPoints[memberId] ?: 0L
        println("[PointService] 현재 잔액: $balance")
        return balance
    }
    
    /**
     * 포인트 사용
     */
    suspend fun usePoint(
        memberId: Long,
        amount: Int,
        description: String,
        referenceId: String
    ): Boolean {
        println("[PointService] 포인트 사용 - 회원: $memberId, 금액: $amount, 설명: $description")
        
        val currentBalance = memberPoints[memberId] ?: 0L
        
        if (currentBalance < amount) {
            println("[PointService] 포인트 부족 - 현재: $currentBalance, 요청: $amount")
            return false
        }
        
        // 실패 시뮬레이션 (3% 확률)
        if (Random.nextDouble() < 0.03) {
            println("[PointService] 포인트 사용 처리 실패")
            return false
        }
        
        // 포인트 차감
        memberPoints[memberId] = currentBalance - amount
        println("[PointService] 포인트 사용 성공 - 잔액: ${memberPoints[memberId]}")
        
        return true
    }
    
    /**
     * 포인트 환불
     */
    suspend fun refundPoint(
        memberId: Long,
        amount: Int,
        description: String,
        referenceId: String
    ): Boolean {
        println("[PointService] 포인트 환불 - 회원: $memberId, 금액: $amount, 설명: $description")
        
        // 실패 시뮬레이션 (2% 확률)
        if (Random.nextDouble() < 0.02) {
            println("[PointService] 포인트 환불 처리 실패")
            return false
        }
        
        val currentBalance = memberPoints[memberId] ?: 0L
        memberPoints[memberId] = currentBalance + amount
        
        println("[PointService] 포인트 환불 성공 - 잔액: ${memberPoints[memberId]}")
        return true
    }
    
    /**
     * 포인트 충전 (테스트용)
     */
    fun chargePoint(memberId: Long, amount: Long) {
        val currentBalance = memberPoints[memberId] ?: 0L
        memberPoints[memberId] = currentBalance + amount
        println("[PointService] 포인트 충전 - 회원: $memberId, 충전액: $amount, 잔액: ${memberPoints[memberId]}")
    }
}
