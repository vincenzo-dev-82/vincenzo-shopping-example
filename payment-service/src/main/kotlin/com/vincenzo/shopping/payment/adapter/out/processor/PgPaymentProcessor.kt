package com.vincenzo.shopping.payment.adapter.out.processor

import com.vincenzo.shopping.payment.application.processor.PaymentProcessor
import com.vincenzo.shopping.payment.application.processor.PaymentResult
import com.vincenzo.shopping.payment.application.processor.ValidationResult
import com.vincenzo.shopping.payment.domain.PaymentDetail
import com.vincenzo.shopping.payment.domain.PaymentMethod
import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.random.Random

@Component
class PgPaymentProcessor : PaymentProcessor {
    
    override fun getSupportedMethod(): PaymentMethod = PaymentMethod.PG_KPN
    
    override suspend fun process(
        orderId: Long,
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any>
    ): PaymentResult {
        println("PG 결제 처리: 주문 $orderId, $amount 원")
        
        // PG 결제 시뮬레이션 (90% 성공률)
        return if (Random.nextDouble() < 0.9) {
            PaymentResult(
                success = true,
                transactionId = "PG_${UUID.randomUUID()}",
                message = "PG 결제 성공",
                processedAmount = amount,
                metadata = mapOf(
                    "pg_response_code" to "0000",
                    "card_name" to "신한카드"
                )
            )
        } else {
            PaymentResult(
                success = false,
                message = "PG 결제 실패: 카드 잔액 부족",
                metadata = mapOf("pg_response_code" to "E001")
            )
        }
    }
    
    override suspend fun cancel(
        paymentDetail: PaymentDetail,
        reason: String
    ): PaymentResult {
        println("PG 결제 취소: ${paymentDetail.transactionId}, 사유: $reason")
        
        return PaymentResult(
            success = true,
            transactionId = "PG_CANCEL_${UUID.randomUUID()}",
            message = "PG 결제 취소 성공",
            processedAmount = paymentDetail.amount
        )
    }
    
    override suspend fun validate(
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any>
    ): ValidationResult {
        // PG는 실제 결제 시점에 검증하므로 사전 검증은 항상 성공
        return ValidationResult(
            isValid = true,
            message = "PG 결제 가능"
        )
    }
}