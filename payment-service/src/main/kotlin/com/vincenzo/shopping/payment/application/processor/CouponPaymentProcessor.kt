package com.vincenzo.shopping.payment.application.processor

import com.vincenzo.shopping.payment.domain.PaymentDetail
import com.vincenzo.shopping.payment.domain.PaymentMethod
import org.springframework.stereotype.Component

@Component
class CouponPaymentProcessor : PaymentProcessor {
    
    // 임시 쿠폰 데이터 (실제로는 DB에서 관리)
    private val coupons = mutableMapOf(
        "WELCOME10" to CouponInfo("WELCOME10", 10000, true),
        "SAVE20" to CouponInfo("SAVE20", 20000, true),
        "VIP50" to CouponInfo("VIP50", 50000, true)
    )
    
    override fun getSupportedMethod(): PaymentMethod = PaymentMethod.COUPON
    
    override suspend fun process(
        orderId: Long,
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any>
    ): PaymentResult {
        println("[COUPON] 쿠폰 할인 처리 시작 - 회원: $memberId, 쿠폰코드: ${metadata["couponCode"]}")
        
        val couponCode = metadata["couponCode"] as? String
            ?: return PaymentResult(
                success = false,
                message = "쿠폰 코드가 필요합니다."
            )
        
        val coupon = coupons[couponCode]
            ?: return PaymentResult(
                success = false,
                message = "유효하지 않은 쿠폰입니다: $couponCode"
            )
        
        if (!coupon.isActive) {
            return PaymentResult(
                success = false,
                message = "이미 사용된 쿠폰입니다: $couponCode"
            )
        }
        
        // 쿠폰 사용 처리
        coupon.isActive = false
        
        val discountAmount = minOf(coupon.discountAmount, amount)
        
        return PaymentResult(
            success = true,
            transactionId = "COUPON-$orderId-$couponCode",
            message = "쿠폰 할인 적용: ${discountAmount}원",
            processedAmount = discountAmount,
            metadata = mapOf(
                "couponCode" to couponCode,
                "discountAmount" to discountAmount,
                "originalAmount" to amount
            )
        )
    }
    
    override suspend fun cancel(
        paymentDetail: PaymentDetail,
        reason: String
    ): PaymentResult {
        println("[COUPON] 쿠폰 할인 취소 - 거래ID: ${paymentDetail.transactionId}")
        
        val couponCode = paymentDetail.metadata["couponCode"] as? String
            ?: return PaymentResult(
                success = false,
                message = "쿠폰 코드를 찾을 수 없습니다."
            )
        
        // 쿠폰 복원
        coupons[couponCode]?.let { it.isActive = true }
        
        return PaymentResult(
            success = true,
            transactionId = "CANCEL-${paymentDetail.transactionId}",
            message = "쿠폰 할인 취소 완료",
            processedAmount = -paymentDetail.amount,
            metadata = mapOf(
                "restoredCoupon" to couponCode
            )
        )
    }
    
    override suspend fun validate(
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any>
    ): ValidationResult {
        // 쿠폰은 단독 결제 불가
        return ValidationResult(
            isValid = false,
            message = "쿠폰은 단독 결제가 불가능합니다. 다른 결제 수단과 함께 사용해주세요."
        )
    }
    
    data class CouponInfo(
        val code: String,
        val discountAmount: Long,
        var isActive: Boolean
    )
}