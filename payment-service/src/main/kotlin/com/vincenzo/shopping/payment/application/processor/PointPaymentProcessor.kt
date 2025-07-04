package com.vincenzo.shopping.payment.application.processor

import com.vincenzo.shopping.payment.adapter.out.grpc.PointServiceGrpcClient
import com.vincenzo.shopping.payment.domain.PaymentDetail
import com.vincenzo.shopping.payment.domain.PaymentMethod
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class PointPaymentProcessor(
    private val pointServiceGrpcClient: PointServiceGrpcClient
) : PaymentProcessor {
    
    override fun getSupportedMethod(): PaymentMethod = PaymentMethod.CASHNOTE_POINT
    
    override suspend fun process(
        orderId: Long,
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any>
    ): PaymentResult {
        println("포인트 결제 처리: 회원 $memberId, $amount 포인트")
        
        // 포인트 잔액 확인
        val balance = pointServiceGrpcClient.getBalance(memberId)
            ?: return PaymentResult(
                success = false,
                message = "포인트 잔액 조회 실패"
            )
        
        if (balance < amount) {
            return PaymentResult(
                success = false,
                message = "포인트가 부족합니다. 현재 잔액: $balance"
            )
        }
        
        // 포인트 사용
        val success = pointServiceGrpcClient.usePoint(
            memberId = memberId,
            amount = amount.toInt(),
            description = "주문 결제: $orderId",
            referenceId = orderId.toString()
        )
        
        return if (success) {
            PaymentResult(
                success = true,
                transactionId = "POINT_${UUID.randomUUID()}",
                message = "포인트 결제 성공",
                processedAmount = amount,
                metadata = mapOf(
                    "remaining_balance" to (balance - amount)
                )
            )
        } else {
            PaymentResult(
                success = false,
                message = "포인트 사용 처리 실패"
            )
        }
    }
    
    override suspend fun cancel(
        paymentDetail: PaymentDetail,
        reason: String
    ): PaymentResult {
        val memberId = paymentDetail.metadata["memberId"] as? Long
            ?: return PaymentResult(
                success = false,
                message = "회원 ID가 없습니다."
            )
        
        val orderId = paymentDetail.metadata["orderId"] as? Long
            ?: return PaymentResult(
                success = false,
                message = "주문 ID가 없습니다."
            )
        
        println("포인트 결제 취소: ${paymentDetail.transactionId}, 사유: $reason")
        
        val success = pointServiceGrpcClient.refundPoint(
            memberId = memberId,
            amount = paymentDetail.amount.toInt(),
            description = "주문 취소: $orderId - $reason",
            referenceId = orderId.toString()
        )
        
        return if (success) {
            PaymentResult(
                success = true,
                transactionId = "POINT_CANCEL_${UUID.randomUUID()}",
                message = "포인트 결제 취소 성공",
                processedAmount = paymentDetail.amount
            )
        } else {
            PaymentResult(
                success = false,
                message = "포인트 환불 처리 실패"
            )
        }
    }
    
    override suspend fun validate(
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any>
    ): ValidationResult {
        println("포인트 결제 검증: 회원 $memberId, $amount 포인트")
        
        val balance = pointServiceGrpcClient.getBalance(memberId)
            ?: return ValidationResult(
                isValid = false,
                message = "포인트 잔액 조회 실패"
            )
        
        return if (balance >= amount) {
            ValidationResult(
                isValid = true,
                availableAmount = balance
            )
        } else {
            ValidationResult(
                isValid = false,
                message = "포인트가 부족합니다. 현재 잔액: $balance, 필요: $amount",
                availableAmount = balance
            )
        }
    }
}
