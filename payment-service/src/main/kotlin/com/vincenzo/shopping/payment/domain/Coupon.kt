package com.vincenzo.shopping.payment.domain

/**
 * 쿠폰 도메인 모델
 */
data class Coupon(
    val id: Long,
    val code: String,
    val discountAmount: Long,
    val minOrderAmount: Long,
    val maxDiscountAmount: Long? = null,
    val isUsed: Boolean = false
)