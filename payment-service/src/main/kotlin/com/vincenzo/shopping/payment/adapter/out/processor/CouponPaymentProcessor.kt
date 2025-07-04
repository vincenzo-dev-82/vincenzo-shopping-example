package com.vincenzo.shopping.payment.adapter.out.processor

import com.vincenzo.shopping.payment.application.processor.PaymentProcessor
import com.vincenzo.shopping.payment.application.processor.PaymentResult
import com.vincenzo.shopping.payment.application.processor.ValidationResult
import com.vincenzo.shopping.payment.domain.PaymentDetail
import com.vincenzo.shopping.payment.domain.PaymentMethod
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CouponPaymentProcessor : PaymentProcessor {
    
    // 테스트용 쿠폰 데이터
    private val coupons = mapOf(
        "WELCOME10" to 10000L,
        "SPECIAL20" to 20000L,
        "VIP50" to 50000L
    )
    
    override fun getSupportedMethod(): PaymentMethod = PaymentMethod.COUPON
    
    override suspend fun process(
        orderId: Long,
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any>
    ): PaymentResult {
        val couponCode = metadata["couponCode"]?.toString()
            ?: return PaymentResult(
                success = false,
                message = "쿠폰 코드가 없습니다."
            )
        
        val discountAmount = coupons[couponCode]
            ?: return PaymentResult(
                success = false,
                message = "유효하지 않은 쿠폰입니다: $couponCode"
            )
        
        return PaymentResult(
            success = true,
            transactionId = "COUPON_${UUID.randomUUID()}",
            message = "쿠폰 적용 성공",
            processedAmount = minOf(amount, discountAmount),
            metadata = mapOf(
                "coupon_code" to couponCode,
                "discount_amount" to discountAmount
            )
        )
    }
    
    override suspend fun cancel(
        paymentDetail: PaymentDetail,
        reason: String
    ): PaymentResult {
        println("쿠폰 사용 취소: ${paymentDetail.transactionId}")
        
        return PaymentResult(
            success = true,
            transactionId = "COUPON_CANCEL_${UUID.randomUUID()}",
            message = "쿠폰 사용 취소 성공",
            processedAmount = paymentDetail.amount
        )
    }
    
    override suspend fun validate(
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any>
    ): ValidationResult {
        val couponCode = metadata["couponCode"]?.toString()
            ?: return ValidationResult(
                isValid = false,
                message = "쿠폰 코드가 없습니다."
            )
        
        val discountAmount = coupons[couponCode]
            ?: return ValidationResult(
                isValid = false,
                message = "유효하지 않은 쿠폰입니다: $couponCode"
            )
        
        return ValidationResult(
            isValid = true,
            message = "쿠폰 사용 가능",
            availableAmount = discountAmount
        )
    }
}