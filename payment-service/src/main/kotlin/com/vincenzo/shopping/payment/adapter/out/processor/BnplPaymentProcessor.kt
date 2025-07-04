package com.vincenzo.shopping.payment.adapter.out.processor

import com.vincenzo.shopping.payment.adapter.out.mock.BnplMockClient
import com.vincenzo.shopping.payment.application.port.out.PaymentProcessor
import com.vincenzo.shopping.payment.application.port.out.PaymentResult
import com.vincenzo.shopping.payment.application.port.out.ValidationResult
import com.vincenzo.shopping.payment.domain.PaymentDetail
import com.vincenzo.shopping.payment.domain.PaymentMethod
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class BnplPaymentProcessor(
    private val bnplMockClient: BnplMockClient
) : PaymentProcessor {
    
    override fun getSupportedMethod(): PaymentMethod = PaymentMethod.BNPL
    
    override suspend fun process(
        orderId: Long,
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any>
    ): PaymentResult {
        println("BNPL 결제 처리: 주문 $orderId, $amount 원")
        
        // BNPL 신청
        val bnplResult = bnplMockClient.applyBnpl(
            orderId = orderId,
            memberId = memberId,
            amount = amount
        )
        
        return if (bnplResult.approved) {
            PaymentResult(
                success = true,
                transactionId = bnplResult.bnplId ?: "BNPL_${UUID.randomUUID()}",
                message = "BNPL 승인 성공",
                processedAmount = amount,
                metadata = mapOf(
                    "due_date" to bnplResult.dueDate,
                    "credit_limit" to bnplResult.creditLimit
                )
            )
        } else {
            PaymentResult(
                success = false,
                message = "BNPL 승인 실패: ${bnplResult.message}"
            )
        }
    }
    
    override suspend fun cancel(
        paymentDetail: PaymentDetail,
        reason: String
    ): PaymentResult {
        println("BNPL 취소: ${paymentDetail.transactionId}")
        
        val success = bnplMockClient.cancelBnpl(
            bnplId = paymentDetail.transactionId ?: "",
            reason = reason
        )
        
        return if (success) {
            PaymentResult(
                success = true,
                transactionId = "BNPL_CANCEL_${UUID.randomUUID()}",
                message = "BNPL 취소 성공",
                processedAmount = paymentDetail.amount
            )
        } else {
            PaymentResult(
                success = false,
                message = "BNPL 취소 실패"
            )
        }
    }
    
    override suspend fun validate(
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any>
    ): ValidationResult {
        println("BNPL 가능 여부 확인: 회원 $memberId, $amount 원")
        
        val creditCheck = bnplMockClient.checkCredit(memberId)
        
        return if (creditCheck.available && creditCheck.availableAmount >= amount) {
            ValidationResult(
                isValid = true,
                message = "BNPL 사용 가능",
                availableAmount = creditCheck.availableAmount
            )
        } else {
            ValidationResult(
                isValid = false,
                message = "BNPL 한도 부족 또는 신용 불가",
                availableAmount = creditCheck.availableAmount
            )
        }
    }
}