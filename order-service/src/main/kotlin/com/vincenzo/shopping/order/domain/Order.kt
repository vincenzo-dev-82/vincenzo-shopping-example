package com.vincenzo.shopping.order.domain

import java.time.LocalDateTime

data class Order(
    val id: Long? = null,
    val memberId: Long,
    val orderItems: List<OrderItem>,
    val totalAmount: Long,
    val status: OrderStatus = OrderStatus.PENDING,
    val paymentMethod: PaymentMethod,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class OrderItem(
    val productId: Long,
    val productName: String,
    val price: Long,
    val quantity: Int
)

enum class OrderStatus {
    PENDING,
    PAID,
    CANCELLED,
    COMPLETED
}

enum class PaymentMethod {
    PG_KPN,
    CASHNOTE_POINT,
    BNPL,
    COMPOSITE  // 복합결제
}
