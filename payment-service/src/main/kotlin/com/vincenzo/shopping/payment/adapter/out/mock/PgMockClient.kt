package com.vincenzo.shopping.payment.adapter.out.mock

import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class PgMockClient {
    
    private val successRate = 0.9 // 90% 성공률
    
    fun processPayment(
        orderId: Long,
        amount: Long,
        cardNumber: String
    ): PgResponse {
        println("[PG Mock] 결제 요청 - 주문: $orderId, 금액: $amount, 카드: $cardNumber")
        
        // 랜덤하게 성공/실패 결정
        val isSuccess = Random.nextDouble() < successRate
        
        return if (isSuccess) {
            PgResponse(
                success = true,
                transactionId = "PG-${System.currentTimeMillis()}-$orderId",
                message = "결제 승인",
                approvalNumber = generateApprovalNumber()
            )
        } else {
            val failureReasons = listOf(
                "카드 한도 초과",
                "카드 정보 오류",
                "3D Secure 인증 실패",
                "카드사 점검 중"
            )
            PgResponse(
                success = false,
                message = failureReasons.random()
            )
        }
    }
    
    fun cancelPayment(
        transactionId: String,
        amount: Long,
        reason: String
    ): PgResponse {
        println("[PG Mock] 결제 취소 요청 - 거래ID: $transactionId, 금액: $amount, 사유: $reason")
        
        // 취소는 95% 성공
        val isSuccess = Random.nextDouble() < 0.95
        
        return if (isSuccess) {
            PgResponse(
                success = true,
                transactionId = "CANCEL-$transactionId",
                message = "결제 취소 완료",
                approvalNumber = generateApprovalNumber()
            )
        } else {
            PgResponse(
                success = false,
                message = "취소 불가능한 거래입니다."
            )
        }
    }
    
    private fun generateApprovalNumber(): String {
        return (100000..999999).random().toString()
    }
}

data class PgResponse(
    val success: Boolean,
    val transactionId: String? = null,
    val message: String,
    val approvalNumber: String? = null
)