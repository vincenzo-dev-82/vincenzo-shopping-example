package com.vincenzo.shopping.point.application.port.out

import com.vincenzo.shopping.point.domain.PointTransaction

interface PointTransactionRepository {
    fun save(transaction: PointTransaction): PointTransaction
    fun findByMemberId(memberId: Long): List<PointTransaction>
}