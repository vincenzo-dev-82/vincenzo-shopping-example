package com.vincenzo.shopping.payment.adapter.out.mock

import org.springframework.stereotype.Component
import java.time.LocalDate
import kotlin.random.Random

@Component
class BnplMockClient {
    
    // 회원별 신용 정보 (실제로는 외부 서비스에서 조회)
    private val creditScores = mutableMapOf<Long, Int>()
    
    fun checkCredit(memberId: Long): CreditCheckResult {
        println("[BNPL Mock] 신용도 조회 - 회원: $memberId")
        
        // 신용 점수 생성 (300-850)
        val creditScore = creditScores.getOrPut(memberId) {
            Random.nextInt(300, 851)
        }
        
        // 신용 점수에 따른 한도 계산
        val (available, creditLimit, reason) = when {
            creditScore >= 700 -> Triple(true, 5000000L, "우수 신용")
            creditScore >= 600 -> Triple(true, 3000000L, "양호 신용")
            creditScore >= 500 -> Triple(true, 1000000L, "보통 신용")
            else -> Triple(false, 0L, "신용도 부족 (점수: $creditScore)")
        }
        
        // 기존 사용액 계산 (랜덤)
        val usedAmount = if (available) Random.nextLong(0, creditLimit / 2) else 0L
        
        return CreditCheckResult(
            available = available,
            creditScore = creditScore,
            creditLimit = creditLimit,
            usedAmount = usedAmount,
            availableAmount = creditLimit - usedAmount,
            message = reason
        )
    }
    
    fun applyBnpl(
        orderId: Long,
        memberId: Long,
        amount: Long,
        installmentMonths: Int = 3
    ): BnplResult {
        println("[BNPL Mock] 후불결제 요청 - 주문: $orderId, 회원: $memberId, 금액: $amount")
        
        // 신용도 재확인
        val creditCheck = checkCredit(memberId)
        
        if (!creditCheck.available) {
            return BnplResult(
                approved = false,
                message = "신용도 부족: ${creditCheck.message}"
            )
        }
        
        if (amount > creditCheck.availableAmount) {
            return BnplResult(
                approved = false,
                message = "한도 초과 (가능: ${creditCheck.availableAmount}, 요청: $amount)"
            )
        }
        
        // 90% 성공률
        val isSuccess = Random.nextDouble() < 0.9
        
        return if (isSuccess) {
            val dueDate = LocalDate.now().plusMonths(1)
            BnplResult(
                approved = true,
                bnplId = "BNPL-${System.currentTimeMillis()}-$orderId",
                message = "후불결제 승인",
                dueDate = dueDate.toString(),
                creditLimit = creditCheck.creditLimit
            )
        } else {
            BnplResult(
                approved = false,
                message = "후불결제 심사 거절"
            )
        }
    }
    
    fun cancelBnpl(
        bnplId: String,
        reason: String
    ): Boolean {
        println("[BNPL Mock] 후불결제 취소 - ID: $bnplId, 사유: $reason")
        return true
    }
}

data class CreditCheckResult(
    val available: Boolean,
    val creditScore: Int,
    val creditLimit: Long,
    val usedAmount: Long,
    val availableAmount: Long,
    val message: String
)

data class BnplResult(
    val approved: Boolean,
    val bnplId: String? = null,
    val message: String,
    val dueDate: String? = null,
    val creditLimit: Long? = null
)