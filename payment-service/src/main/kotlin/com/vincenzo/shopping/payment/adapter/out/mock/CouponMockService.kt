package com.vincenzo.shopping.payment.adapter.out.mock

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class CouponMockService {
    
    // 테스트용 쿠폰 데이터
    private val coupons = ConcurrentHashMap<String, CouponInfo>().apply {
        put("WELCOME1000", CouponInfo("WELCOME1000", 1000, 5000))
        put("SAVE5000", CouponInfo("SAVE5000", 5000, 20000))
        put("VIP10000", CouponInfo("VIP10000", 10000, 50000))
        put("SPECIAL3000", CouponInfo("SPECIAL3000", 3000, 10000))
    }
    
    // 사용된 쿠폰 추적
    private val usedCoupons = ConcurrentHashMap<String, Long>()
    
    fun validateCoupon(couponCode: String, memberId: Long): CouponValidationResult {
        println("[쿠폰 Mock] 쿠폰 검증 - 코드: $couponCode, 회원: $memberId")
        
        val coupon = coupons[couponCode]
            ?: return CouponValidationResult(
                valid = false,
                message = "존재하지 않는 쿠폰입니다"
            )
        
        // 이미 사용된 쿠폰인지 확인
        if (usedCoupons.containsKey(couponCode)) {
            return CouponValidationResult(
                valid = false,
                message = "이미 사용된 쿠폰입니다"
            )
        }
        
        return CouponValidationResult(
            valid = true,
            message = "사용 가능한 쿠폰입니다",
            discountAmount = coupon.discountAmount,
            minOrderAmount = coupon.minOrderAmount
        )
    }
    
    fun useCoupon(
        couponCode: String,
        memberId: Long,
        orderAmount: Long
    ): CouponUseResult {
        val coupon = coupons[couponCode]
            ?: return CouponUseResult(
                success = false,
                message = "존재하지 않는 쿠폰입니다"
            )
        
        if (orderAmount < coupon.minOrderAmount) {
            return CouponUseResult(
                success = false,
                message = "최소 주문금액 ${coupon.minOrderAmount}원 이상 사용 가능합니다"
            )
        }
        
        usedCoupons[couponCode] = memberId
        
        return CouponUseResult(
            success = true,
            message = "쿠폰 적용 성공",
            discountAmount = coupon.discountAmount,
            discountRate = (coupon.discountAmount.toDouble() / orderAmount * 100).toInt()
        )
    }
    
    fun cancelCoupon(couponCode: String, transactionId: String): Boolean {
        usedCoupons.remove(couponCode)
        println("[쿠폰 Mock] 쿠폰 사용 취소 - 코드: $couponCode")
        return true
    }
}

data class CouponInfo(
    val code: String,
    val discountAmount: Long,
    val minOrderAmount: Long
)

data class CouponValidationResult(
    val valid: Boolean,
    val message: String,
    val discountAmount: Long? = null,
    val minOrderAmount: Long? = null
)

data class CouponUseResult(
    val success: Boolean,
    val message: String,
    val discountAmount: Long? = null,
    val discountRate: Int? = null
)