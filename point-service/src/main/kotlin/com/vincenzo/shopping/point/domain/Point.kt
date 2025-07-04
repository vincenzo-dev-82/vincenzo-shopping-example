package com.vincenzo.shopping.point.domain

import java.time.LocalDateTime

data class PointBalance(
    val id: Long? = null,
    val memberId: Long,
    val balance: Int,
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class PointTransaction(
    val id: Long? = null,
    val memberId: Long,
    val amount: Int,
    val type: TransactionType,
    val description: String,
    val referenceId: String? = null,  // 주문 ID 등 참조
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class TransactionType {
    CHARGE,     // 충전
    USE,        // 사용
    REFUND,     // 환불
    EXPIRE      // 만료
}