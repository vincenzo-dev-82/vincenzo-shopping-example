package com.vincenzo.shopping.payment.adapter.out.processor

import com.vincenzo.shopping.payment.domain.PaymentDetail
import com.vincenzo.shopping.payment.domain.PaymentMethod
import com.vincenzo.shopping.payment.domain.PaymentProcessor
import com.vincenzo.shopping.payment.domain.PaymentResult
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CouponPaymentProcessor : PaymentProcessor {
    
    // Mock 쿠폰 데이터
    private val validCoupons = mapOf(
        "WELCOME10" to CouponInfo(10000, "신규 가입 쿠폰"),
        "SUMMER20" to CouponInfo(20000, "여름 특별 할인"),
        "VIP50" to CouponInfo(50000, "VIP 고객 쿠폰")
    )
    
    override fun process(paymentDetail: PaymentDetail): PaymentResult {
        val couponCode = paymentDetail.metadata["coupon_code"]
            ?: return PaymentResult(
                success = false,
                message = "쿠폰 코드가 없습니다."
            )
        
        println("쿠폰 적용: $couponCode, 할인액 ${paymentDetail.amount}원")
        
        val coupon = validCoupons[couponCode]
            ?: return PaymentResult(
                success = false,
                message = "유효하지 않은 쿠폰입니다."
            )
        
        if (coupon.discountAmount != paymentDetail.amount.toInt()) {
            return PaymentResult(
                success = false,
                message = "쿠폰 할인 금액이 일치하지 않습니다."
            )
        }
        
        return PaymentResult(
            success = true,
            transactionId = "COUPON_${UUID.randomUUID()}",
            message = "쿠폰 적용 성공",
            metadata = mapOf(
                "coupon_name" to coupon.name,
                "discount_amount" to coupon.discountAmount.toString()
            )
        )
    }
    
    override fun cancel(paymentDetail: PaymentDetail): PaymentResult {
        println("쿠폰 사용 취소: ${paymentDetail.transactionId}")
        
        return PaymentResult(
            success = true,
            transactionId = "COUPON_CANCEL_${UUID.randomUUID()}",
            message = "쿠폰 사용 취소 성공"
        )
    }
    
    override fun refund(paymentDetail: PaymentDetail, refundAmount: Long): PaymentResult {
        // 쿠폰은 부분 환불 불가, 전체 취소만 가능
        if (refundAmount != paymentDetail.amount) {
            return PaymentResult(
                success = false,
                message = "쿠폰은 부분 환불이 불가능합니다."
            )
        }
        
        return cancel(paymentDetail)
    }
    
    override fun supports(method: PaymentMethod): Boolean {
        return method == PaymentMethod.COUPON
    }
    
    private data class CouponInfo(
        val discountAmount: Int,
        val name: String
    )
}