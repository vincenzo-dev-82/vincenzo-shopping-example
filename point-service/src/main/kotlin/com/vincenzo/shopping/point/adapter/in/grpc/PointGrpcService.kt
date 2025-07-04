package com.vincenzo.shopping.point.adapter.`in`.grpc

import com.vincenzo.shopping.grpc.point.*
import com.vincenzo.shopping.point.application.port.`in`.*
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class PointGrpcService(
    private val usePointUseCase: UsePointUseCase,
    private val chargePointUseCase: ChargePointUseCase,
    private val refundPointUseCase: RefundPointUseCase,
    private val getPointBalanceQuery: GetPointBalanceQuery
) : PointServiceGrpcKt.PointServiceCoroutineImplBase() {
    
    override suspend fun getBalance(request: GetBalanceRequest): BalanceResponse {
        val balance = getPointBalanceQuery.getBalance(request.memberId)
            ?: return BalanceResponse.newBuilder()
                .setMemberId(request.memberId)
                .setBalance(0)
                .setUpdatedAt("N/A")
                .build()
        
        return BalanceResponse.newBuilder()
            .setMemberId(balance.memberId)
            .setBalance(balance.balance)
            .setUpdatedAt(balance.updatedAt.toString())
            .build()
    }
    
    override suspend fun usePoint(request: UsePointRequest): TransactionResponse {
        val command = UsePointCommand(
            memberId = request.memberId,
            amount = request.amount,
            description = request.description,
            referenceId = request.referenceId.takeIf { it.isNotEmpty() }
        )
        
        val transaction = usePointUseCase.usePoint(command)
        
        return transaction.toGrpcResponse()
    }
    
    override suspend fun chargePoint(request: ChargePointRequest): TransactionResponse {
        val command = ChargePointCommand(
            memberId = request.memberId,
            amount = request.amount,
            description = request.description
        )
        
        val transaction = chargePointUseCase.chargePoint(command)
        
        return transaction.toGrpcResponse()
    }
    
    override suspend fun refundPoint(request: RefundPointRequest): TransactionResponse {
        val command = RefundPointCommand(
            memberId = request.memberId,
            amount = request.amount,
            description = request.description,
            referenceId = request.referenceId.takeIf { it.isNotEmpty() }
        )
        
        val transaction = refundPointUseCase.refundPoint(command)
        
        return transaction.toGrpcResponse()
    }
}

private fun com.vincenzo.shopping.point.domain.PointTransaction.toGrpcResponse(): TransactionResponse {
    return TransactionResponse.newBuilder()
        .setId(this.id ?: 0)
        .setMemberId(this.memberId)
        .setAmount(this.amount)
        .setType(this.type.name)
        .setDescription(this.description)
        .setReferenceId(this.referenceId ?: "")
        .setCreatedAt(this.createdAt.toString())
        .build()
}