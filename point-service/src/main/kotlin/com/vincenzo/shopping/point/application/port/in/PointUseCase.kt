package com.vincenzo.shopping.point.application.port.`in`

import com.vincenzo.shopping.point.domain.PointBalance
import com.vincenzo.shopping.point.domain.PointTransaction

interface UsePointUseCase {
    fun usePoint(command: UsePointCommand): PointTransaction
}

data class UsePointCommand(
    val memberId: Long,
    val amount: Int,
    val description: String,
    val referenceId: String? = null
)

interface ChargePointUseCase {
    fun chargePoint(command: ChargePointCommand): PointTransaction
}

data class ChargePointCommand(
    val memberId: Long,
    val amount: Int,
    val description: String
)

interface RefundPointUseCase {
    fun refundPoint(command: RefundPointCommand): PointTransaction
}

data class RefundPointCommand(
    val memberId: Long,
    val amount: Int,
    val description: String,
    val referenceId: String? = null
)

interface GetPointBalanceQuery {
    fun getBalance(memberId: Long): PointBalance?
    fun getTransactionHistory(memberId: Long): List<PointTransaction>
}