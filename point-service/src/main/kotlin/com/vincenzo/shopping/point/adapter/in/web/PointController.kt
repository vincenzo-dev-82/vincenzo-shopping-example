package com.vincenzo.shopping.point.adapter.`in`.web

import com.vincenzo.shopping.point.application.port.`in`.*
import com.vincenzo.shopping.point.domain.PointBalance
import com.vincenzo.shopping.point.domain.PointTransaction
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/points")
class PointController(
    private val usePointUseCase: UsePointUseCase,
    private val chargePointUseCase: ChargePointUseCase,
    private val refundPointUseCase: RefundPointUseCase,
    private val getPointBalanceQuery: GetPointBalanceQuery
) {
    
    @GetMapping("/balance/{memberId}")
    fun getBalance(@PathVariable memberId: Long): PointBalanceResponse {
        val balance = getPointBalanceQuery.getBalance(memberId)
            ?: PointBalance(memberId = memberId, balance = 0)
        return PointBalanceResponse.from(balance)
    }
    
    @GetMapping("/transactions/{memberId}")
    fun getTransactionHistory(@PathVariable memberId: Long): List<PointTransactionResponse> {
        return getPointBalanceQuery.getTransactionHistory(memberId)
            .map { PointTransactionResponse.from(it) }
    }
    
    @PostMapping("/charge")
    @ResponseStatus(HttpStatus.CREATED)
    fun chargePoint(@RequestBody request: ChargePointRequest): PointTransactionResponse {
        val command = ChargePointCommand(
            memberId = request.memberId,
            amount = request.amount,
            description = request.description
        )
        val transaction = chargePointUseCase.chargePoint(command)
        return PointTransactionResponse.from(transaction)
    }
    
    @PostMapping("/use")
    @ResponseStatus(HttpStatus.CREATED)
    fun usePoint(@RequestBody request: UsePointRequest): PointTransactionResponse {
        val command = UsePointCommand(
            memberId = request.memberId,
            amount = request.amount,
            description = request.description,
            referenceId = request.referenceId
        )
        val transaction = usePointUseCase.usePoint(command)
        return PointTransactionResponse.from(transaction)
    }
    
    @PostMapping("/refund")
    @ResponseStatus(HttpStatus.CREATED)
    fun refundPoint(@RequestBody request: RefundPointRequest): PointTransactionResponse {
        val command = RefundPointCommand(
            memberId = request.memberId,
            amount = request.amount,
            description = request.description,
            referenceId = request.referenceId
        )
        val transaction = refundPointUseCase.refundPoint(command)
        return PointTransactionResponse.from(transaction)
    }
    
    @GetMapping("/hello")
    fun hello(): String {
        return "Hello from Point Service!"
    }
}

// Request DTOs
data class ChargePointRequest(
    val memberId: Long,
    val amount: Int,
    val description: String
)

data class UsePointRequest(
    val memberId: Long,
    val amount: Int,
    val description: String,
    val referenceId: String? = null
)

data class RefundPointRequest(
    val memberId: Long,
    val amount: Int,
    val description: String,
    val referenceId: String? = null
)

// Response DTOs
data class PointBalanceResponse(
    val memberId: Long,
    val balance: Int,
    val updatedAt: String
) {
    companion object {
        fun from(balance: PointBalance): PointBalanceResponse {
            return PointBalanceResponse(
                memberId = balance.memberId,
                balance = balance.balance,
                updatedAt = balance.updatedAt.toString()
            )
        }
    }
}

data class PointTransactionResponse(
    val id: Long?,
    val memberId: Long,
    val amount: Int,
    val type: String,
    val description: String,
    val referenceId: String?,
    val createdAt: String
) {
    companion object {
        fun from(transaction: PointTransaction): PointTransactionResponse {
            return PointTransactionResponse(
                id = transaction.id,
                memberId = transaction.memberId,
                amount = transaction.amount,
                type = transaction.type.name,
                description = transaction.description,
                referenceId = transaction.referenceId,
                createdAt = transaction.createdAt.toString()
            )
        }
    }
}