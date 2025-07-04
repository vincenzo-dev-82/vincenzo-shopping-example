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
class BnplPaymentProcessor : PaymentProcessor {
    
    override fun getSupportedMethod(): PaymentMethod = PaymentMethod.BNPL
    
    override suspend fun process(
        orderId: Long,
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any>
    ): PaymentResult {
        println("BNPL 결제 처리: 회원 $memberId, $amount 원")
        
        // BNPL 한도 체크 시뮬레이션
        val creditLimit = metadata["creditLimit"]?.toString()?.toLongOrNull() ?: 1000000L
        
        return if (amount <= creditLimit && Random.nextDouble() < 0.85) {
            PaymentResult(
                success = true,
                transactionId = "BNPL_${UUID.randomUUID()}",
                message = "BNPL 결제 승인",
                processedAmount = amount,
                metadata = mapOf(
                    "approval_number" to UUID.randomUUID().toString().substring(0, 8),
                    "due_date" to "2024-03-31"
                )
            )
        } else {
            PaymentResult(
                success = false,
                message = "BNPL 결제 거절: 신용 한도 초과",
                metadata = mapOf("available_limit" to creditLimit)
            )
        }
    }
    
    override suspend fun cancel(
        paymentDetail: PaymentDetail,
        reason: String
    ): PaymentResult {
        println("BNPL 결제 취소: ${paymentDetail.transactionId}, 사유: $reason")
        
        return PaymentResult(
            success = true,
            transactionId = "BNPL_CANCEL_${UUID.randomUUID()}",
            message = "BNPL 결제 취소 성공",
            processedAmount = paymentDetail.amount
        )
    }
    
    override suspend fun validate(
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any>
    ): ValidationResult {
        val creditLimit = metadata["creditLimit"]?.toString()?.toLongOrNull() ?: 1000000L
        
        return if (amount <= creditLimit) {
            ValidationResult(
                isValid = true,
                message = "BNPL 결제 가능",
                availableAmount = creditLimit
            )
        } else {
            ValidationResult(
                isValid = false,
                message = "BNPL 한도 초과",
                availableAmount = creditLimit
            )
        }
    }
}