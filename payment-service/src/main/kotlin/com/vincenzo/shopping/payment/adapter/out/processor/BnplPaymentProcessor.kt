package com.vincenzo.shopping.payment.adapter.out.processor

import com.vincenzo.shopping.payment.domain.PaymentDetail
import com.vincenzo.shopping.payment.domain.PaymentMethod
import com.vincenzo.shopping.payment.domain.PaymentProcessor
import com.vincenzo.shopping.payment.domain.PaymentResult
import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.random.Random

@Component
class BnplPaymentProcessor : PaymentProcessor {
    
    override fun process(paymentDetail: PaymentDetail): PaymentResult {
        val memberId = paymentDetail.metadata["member_id"]
            ?: return PaymentResult(
                success = false,
                message = "회원 ID가 없습니다."
            )
        
        println("BNPL 결제 처리: 회원 $memberId, ${paymentDetail.amount}원")
        
        // Mock BNPL 신용 평가 (80% 승인율)
        val creditCheck = performCreditCheck(memberId.toLong(), paymentDetail.amount)
        
        return if (creditCheck) {
            PaymentResult(
                success = true,
                transactionId = "BNPL_${UUID.randomUUID()}",
                message = "BNPL 결제 승인",
                metadata = mapOf(
                    "credit_limit" to "1000000",
                    "installment_months" to "3",
                    "monthly_payment" to (paymentDetail.amount / 3).toString()
                )
            )
        } else {
            PaymentResult(
                success = false,
                message = "BNPL 신용 평가 실패: 한도 초과"
            )
        }
    }
    
    override fun cancel(paymentDetail: PaymentDetail): PaymentResult {
        println("BNPL 결제 취소: ${paymentDetail.transactionId}")
        
        return PaymentResult(
            success = true,
            transactionId = "BNPL_CANCEL_${UUID.randomUUID()}",
            message = "BNPL 결제 취소 성공"
        )
    }
    
    override fun refund(paymentDetail: PaymentDetail, refundAmount: Long): PaymentResult {
        println("BNPL 환불 처리: ${refundAmount}원")
        
        return PaymentResult(
            success = true,
            transactionId = "BNPL_REFUND_${UUID.randomUUID()}",
            message = "BNPL 환불 성공",
            metadata = mapOf(
                "refund_amount" to refundAmount.toString(),
                "adjusted_installment" to ((paymentDetail.amount - refundAmount) / 3).toString()
            )
        )
    }
    
    override fun supports(method: PaymentMethod): Boolean {
        return method == PaymentMethod.BNPL
    }
    
    private fun performCreditCheck(memberId: Long, amount: Long): Boolean {
        // Mock 신용 평가 로직
        // 실제로는 외부 BNPL 서비스 API 호출
        return Random.nextDouble() < 0.8 && amount <= 1000000
    }
}