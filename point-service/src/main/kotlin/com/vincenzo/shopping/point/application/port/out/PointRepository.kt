package com.vincenzo.shopping.point.application.port.out

import com.vincenzo.shopping.point.domain.PointBalance
import com.vincenzo.shopping.point.domain.PointTransaction

interface PointBalanceRepository {
    fun findByMemberId(memberId: Long): PointBalance?
    fun save(pointBalance: PointBalance): PointBalance
}

interface PointTransactionRepository {
    fun save(transaction: PointTransaction): PointTransaction
    fun findByMemberId(memberId: Long): List<PointTransaction>
}
