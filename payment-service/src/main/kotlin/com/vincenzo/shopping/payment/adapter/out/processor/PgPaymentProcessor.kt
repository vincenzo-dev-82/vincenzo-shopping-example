package com.vincenzo.shopping.payment.adapter.out.processor

import com.vincenzo.shopping.payment.domain.PaymentDetail
import com.vincenzo.shopping.payment.domain.PaymentMethod
import com.vincenzo.shopping.payment.domain.PaymentProcessor
import com.vincenzo.shopping.payment.domain.PaymentResult
import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.random.Random

@Component
class PgPaymentProcessor : PaymentProcessor {
    
    override fun process(paymentDetail: PaymentDetail): PaymentResult {
        println("PG 결제 처리: ${paymentDetail.amount}원")
        
        // Mock PG 결제 처리 (90% 성공률)
        val isSuccess = Random.nextDouble() < 0.9
        
        return if (isSuccess) {
            PaymentResult(
                success = true,
                transactionId = "PG_${UUID.randomUUID()}",
                message = "PG 결제 성공",
                metadata = mapOf(
                    "pg_name" to "KPN",
                    "approval_number" to generateApprovalNumber()
                )
            )
        } else {
            PaymentResult(
                success = false,
                message = "PG 결제 실패: 카드 한도 초과"
            )
        }
    }
    
    override fun cancel(paymentDetail: PaymentDetail): PaymentResult {
        println("PG 결제 취소: ${paymentDetail.transactionId}")
        
        return PaymentResult(
            success = true,
            transactionId = "PG_CANCEL_${UUID.randomUUID()}",
            message = "PG 결제 취소 성공"
        )
    }
    
    override fun refund(paymentDetail: PaymentDetail, refundAmount: Long): PaymentResult {
        println("PG 환불 처리: ${refundAmount}원")
        
        return PaymentResult(
            success = true,
            transactionId = "PG_REFUND_${UUID.randomUUID()}",
            message = "PG 환불 성공",
            metadata = mapOf("refund_amount" to refundAmount.toString())
        )
    }
    
    override fun supports(method: PaymentMethod): Boolean {
        return method == PaymentMethod.PG_KPN
    }
    
    private fun generateApprovalNumber(): String {
        return (100000..999999).random().toString()
    }
}