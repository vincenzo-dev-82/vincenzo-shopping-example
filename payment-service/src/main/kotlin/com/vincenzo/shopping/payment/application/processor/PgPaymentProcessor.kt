package com.vincenzo.shopping.payment.application.processor

import com.vincenzo.shopping.payment.adapter.out.mock.PgMockClient
import com.vincenzo.shopping.payment.domain.PaymentDetail
import com.vincenzo.shopping.payment.domain.PaymentMethod
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class PgPaymentProcessor(
    private val pgMockClient: PgMockClient
) : PaymentProcessor {
    
    override fun getSupportedMethod(): PaymentMethod = PaymentMethod.PG_KPN
    
    override suspend fun process(
        orderId: Long,
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any>
    ): PaymentResult {
        println("[PG] 결제 처리 시작 - 주문: $orderId, 금액: $amount")
        
        // Mock PG 결제 처리
        val pgResult = pgMockClient.processPayment(
            orderId = orderId,
            amount = amount,
            cardNumber = metadata["cardNumber"] as? String ?: "1234-5678-9012-3456"
        )
        
        return PaymentResult(
            success = pgResult.success,
            transactionId = pgResult.transactionId,
            message = pgResult.message,
            processedAmount = if (pgResult.success) amount else 0,
            metadata = mapOf(
                "pgResponse" to pgResult,
                "paymentMethod" to "PG_KPN"
            )
        )
    }
    
    override suspend fun cancel(
        paymentDetail: PaymentDetail,
        reason: String
    ): PaymentResult {
        println("[PG] 결제 취소 - 거래ID: ${paymentDetail.transactionId}, 금액: ${paymentDetail.amount}")
        
        val cancelResult = pgMockClient.cancelPayment(
            transactionId = paymentDetail.transactionId ?: "",
            amount = paymentDetail.amount,
            reason = reason
        )
        
        return PaymentResult(
            success = cancelResult.success,
            transactionId = cancelResult.transactionId,
            message = cancelResult.message,
            processedAmount = if (cancelResult.success) -paymentDetail.amount else 0
        )
    }
    
    override suspend fun validate(
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any>
    ): ValidationResult {
        // PG는 항상 유효 (카드 유효성 검사 등은 실제 결제 시 수행)
        return ValidationResult(
            isValid = true,
            message = "PG 결제 가능",
            availableAmount = amount
        )
    }
}