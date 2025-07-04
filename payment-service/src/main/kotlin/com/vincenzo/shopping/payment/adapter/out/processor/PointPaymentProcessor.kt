package com.vincenzo.shopping.payment.adapter.out.processor

import com.vincenzo.shopping.payment.adapter.out.grpc.PointServiceGrpcClient
import com.vincenzo.shopping.payment.domain.PaymentDetail
import com.vincenzo.shopping.payment.domain.PaymentMethod
import com.vincenzo.shopping.payment.domain.PaymentProcessor
import com.vincenzo.shopping.payment.domain.PaymentResult
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class PointPaymentProcessor(
    private val pointServiceGrpcClient: PointServiceGrpcClient
) : PaymentProcessor {
    
    override fun process(paymentDetail: PaymentDetail): PaymentResult = runBlocking {
        val memberId = paymentDetail.metadata["member_id"]?.toLong()
            ?: return@runBlocking PaymentResult(
                success = false,
                message = "회원 ID가 없습니다."
            )
        
        val orderId = paymentDetail.metadata["order_id"]
            ?: return@runBlocking PaymentResult(
                success = false,
                message = "주문 ID가 없습니다."
            )
        
        println("포인트 결제 처리: 회원 $memberId, ${paymentDetail.amount}포인트")
        
        // 포인트 잔액 확인
        val balance = pointServiceGrpcClient.getBalance(memberId)
            ?: return@runBlocking PaymentResult(
                success = false,
                message = "포인트 잔액 조회 실패"
            )
        
        if (balance < paymentDetail.amount) {
            return@runBlocking PaymentResult(
                success = false,
                message = "포인트가 부족합니다. 현재 잔액: $balance"
            )
        }
        
        // 포인트 사용
        val success = pointServiceGrpcClient.usePoint(
            memberId = memberId,
            amount = paymentDetail.amount.toInt(),
            description = "주문 결제: $orderId",
            referenceId = orderId
        )
        
        if (success) {
            PaymentResult(
                success = true,
                transactionId = "POINT_${UUID.randomUUID()}",
                message = "포인트 결제 성공",
                metadata = mapOf(
                    "remaining_balance" to (balance - paymentDetail.amount).toString()
                )
            )
        } else {
            PaymentResult(
                success = false,
                message = "포인트 사용 처리 실패"
            )
        }
    }
    
    override fun cancel(paymentDetail: PaymentDetail): PaymentResult = runBlocking {
        val memberId = paymentDetail.metadata["member_id"]?.toLong()
            ?: return@runBlocking PaymentResult(
                success = false,
                message = "회원 ID가 없습니다."
            )
        
        val orderId = paymentDetail.metadata["order_id"]
            ?: return@runBlocking PaymentResult(
                success = false,
                message = "주문 ID가 없습니다."
            )
        
        println("포인트 결제 취소: ${paymentDetail.transactionId}")
        
        val success = pointServiceGrpcClient.refundPoint(
            memberId = memberId,
            amount = paymentDetail.amount.toInt(),
            description = "주문 취소: $orderId",
            referenceId = orderId
        )
        
        if (success) {
            PaymentResult(
                success = true,
                transactionId = "POINT_CANCEL_${UUID.randomUUID()}",
                message = "포인트 결제 취소 성공"
            )
        } else {
            PaymentResult(
                success = false,
                message = "포인트 환불 처리 실패"
            )
        }
    }
    
    override fun refund(paymentDetail: PaymentDetail, refundAmount: Long): PaymentResult = runBlocking {
        val memberId = paymentDetail.metadata["member_id"]?.toLong()
            ?: return@runBlocking PaymentResult(
                success = false,
                message = "회원 ID가 없습니다."
            )
        
        val orderId = paymentDetail.metadata["order_id"]
            ?: return@runBlocking PaymentResult(
                success = false,
                message = "주문 ID가 없습니다."
            )
        
        println("포인트 환불 처리: ${refundAmount}포인트")
        
        val success = pointServiceGrpcClient.refundPoint(
            memberId = memberId,
            amount = refundAmount.toInt(),
            description = "부분 환불: $orderId",
            referenceId = orderId
        )
        
        if (success) {
            PaymentResult(
                success = true,
                transactionId = "POINT_REFUND_${UUID.randomUUID()}",
                message = "포인트 환불 성공",
                metadata = mapOf("refund_amount" to refundAmount.toString())
            )
        } else {
            PaymentResult(
                success = false,
                message = "포인트 환불 처리 실패"
            )
        }
    }
    
    override fun supports(method: PaymentMethod): Boolean {
        return method == PaymentMethod.POINT
    }
}