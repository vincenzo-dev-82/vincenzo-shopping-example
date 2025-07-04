package com.vincenzo.shopping.payment.adapter.out.mock

import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class PgMockService {
    
    /**
     * PG 결제 처리
     * - 금액이 1,000,000원 이상이면 실패
     * - 그 외에는 90% 성공률
     */
    fun processPayment(orderId: Long, amount: Long): PgPaymentResult {
        println("[PG Mock] 결제 요청 - 주문ID: $orderId, 금액: $amount")
        
        // 테스트를 위한 실패 조건
        if (amount >= 1_000_000) {
            return PgPaymentResult(
                success = false,
                transactionId = null,
                message = "결제 한도 초과"
            )
        }
        
        // 90% 성공률
        val success = Random.nextDouble() < 0.9
        
        return if (success) {
            PgPaymentResult(
                success = true,
                transactionId = "PG_${System.currentTimeMillis()}",
                message = "결제 성공"
            )
        } else {
            PgPaymentResult(
                success = false,
                transactionId = null,
                message = "PG 서버 오류"
            )
        }
    }
    
    fun cancelPayment(transactionId: String): Boolean {
        println("[PG Mock] 결제 취소 - 거래ID: $transactionId")
        return true
    }
}

data class PgPaymentResult(
    val success: Boolean,
    val transactionId: String?,
    val message: String
)