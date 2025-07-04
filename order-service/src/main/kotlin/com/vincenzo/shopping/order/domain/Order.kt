package com.vincenzo.shopping.order.domain

import java.time.LocalDateTime

data class Order(
    val id: Long? = null,
    val memberId: Long,
    val orderItems: List<OrderItem>,
    val totalAmount: Long,
    val paymentMethod: PaymentMethod,
    val paymentDetails: List<OrderPaymentDetail> = emptyList(),
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class OrderItem(
    val productId: Long,
    val productName: String,
    val price: Long,
    val quantity: Int
)

data class OrderPaymentDetail(
    val method: PaymentMethod,
    val amount: Long,
    val metadata: Map<String, String> = emptyMap()  // 쿠폰 코드 등 추가 정보
)

enum class OrderStatus {
    PENDING,            // 주문 생성
    PAYMENT_PROCESSING, // 결제 진행중
    PAID,              // 결제 완료
    PAYMENT_FAILED,    // 결제 실패
    CANCELLED,         // 주문 취소
    COMPLETED          // 주문 완료
}

enum class PaymentMethod {
    PG_KPN,         // PG 결제
    POINT,          // 캐시노트 포인트
    COUPON,         // 쿠폰
    BNPL            // 외상결제
}