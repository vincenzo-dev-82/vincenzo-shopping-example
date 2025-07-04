package com.vincenzo.shopping.point.application.port.out

import com.vincenzo.shopping.point.domain.PointBalance

interface PointBalanceRepository {
    fun findByMemberId(memberId: Long): PointBalance?
    fun save(pointBalance: PointBalance): PointBalance
}