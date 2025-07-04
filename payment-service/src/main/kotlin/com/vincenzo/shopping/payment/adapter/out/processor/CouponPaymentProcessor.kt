package com.vincenzo.shopping.payment.adapter.out.processor

import com.vincenzo.shopping.payment.adapter.out.mock.CouponMockService
import com.vincenzo.shopping.payment.application.port.out.PaymentProcessor
import com.vincenzo.shopping.payment.application.port.out.PaymentResult
import com.vincenzo.shopping.payment.application.port.out.ValidationResult
import com.vincenzo.shopping.payment.domain.PaymentDetail
import com.vincenzo.shopping.payment.domain.PaymentMethod
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CouponPaymentProcessor(
    private val couponMockService: CouponMockService
) : PaymentProcessor {
    
    override fun getSupportedMethod(): PaymentMethod = PaymentMethod.COUPON
    
    override suspend fun process(
        orderId: Long,
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any>
    ): PaymentResult {
        val couponCode = metadata["coupon_code"]?.toString()
            ?: return PaymentResult(
                success = false,
                message = "쿠폰 코드가 없습니다."
            )
        
        println("쿠폰 적용: $couponCode, 금액: $amount")
        
        val useResult = couponMockService.useCoupon(
            couponCode = couponCode,
            memberId = memberId,
            orderAmount = amount
        )
        
        return if (useResult.success) {
            PaymentResult(
                success = true,
                transactionId = "COUPON_${UUID.randomUUID()}",
                message = "쿠폰 적용 성공",
                processedAmount = useResult.discountAmount,
                metadata = mapOf(
                    "coupon_code" to couponCode,
                    "discount_rate" to useResult.discountRate,
                    "discount_amount" to useResult.discountAmount
                )
            )
        } else {
            PaymentResult(
                success = false,
                message = useResult.message
            )
        }
    }
    
    override suspend fun cancel(
        paymentDetail: PaymentDetail,
        reason: String
    ): PaymentResult {
        val couponCode = paymentDetail.metadata["coupon_code"]?.toString()
            ?: return PaymentResult(
                success = false,
                message = "쿠폰 코드가 없습니다."
            )
        
        println("쿠폰 취소: $couponCode")
        
        val success = couponMockService.cancelCoupon(
            couponCode = couponCode,
            transactionId = paymentDetail.transactionId ?: ""
        )
        
        return if (success) {
            PaymentResult(
                success = true,
                transactionId = "COUPON_CANCEL_${UUID.randomUUID()}",
                message = "쿠폰 취소 성공",
                processedAmount = paymentDetail.amount
            )
        } else {
            PaymentResult(
                success = false,
                message = "쿠폰 취소 실패"
            )
        }
    }
    
    override suspend fun validate(
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any>
    ): ValidationResult {
        val couponCode = metadata["coupon_code"]?.toString()
            ?: return ValidationResult(
                isValid = false,
                message = "쿠폰 코드가 없습니다."
            )
        
        println("쿠폰 검증: $couponCode")
        
        val validation = couponMockService.validateCoupon(
            couponCode = couponCode,
            memberId = memberId
        )
        
        return if (validation.valid) {
            ValidationResult(
                isValid = true,
                message = "유효한 쿠폰"
            )
        } else {
            ValidationResult(
                isValid = false,
                message = validation.message
            )
        }
    }
}