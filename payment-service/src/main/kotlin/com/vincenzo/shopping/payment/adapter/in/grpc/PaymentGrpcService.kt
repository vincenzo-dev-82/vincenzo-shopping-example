package com.vincenzo.shopping.payment.adapter.`in`.grpc

import com.vincenzo.shopping.grpc.payment.*
import com.vincenzo.shopping.payment.application.port.`in`.*
import com.vincenzo.shopping.payment.domain.PaymentMethod
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class PaymentGrpcService(
    private val processPaymentUseCase: ProcessPaymentUseCase,
    private val getPaymentQuery: GetPaymentQuery
) : PaymentServiceGrpcKt.PaymentServiceCoroutineImplBase() {
    
    override suspend fun processPayment(request: ProcessPaymentRequest): PaymentResponse {
        val command = ProcessPaymentCommand(
            orderId = request.orderId,
            memberId = request.memberId,
            totalAmount = request.totalAmount,
            paymentDetails = request.paymentDetailsList.map { detail ->
                PaymentDetailCommand(
                    method = PaymentMethod.valueOf(detail.method),
                    amount = detail.amount,
                    metadata = detail.metadataMap
                )
            }
        )
        
        val payment = processPaymentUseCase.processPayment(command)
        
        return payment.toGrpcResponse()
    }
    
    override suspend fun getPaymentByOrderId(request: GetPaymentByOrderIdRequest): PaymentResponse {
        val payment = getPaymentQuery.getPaymentByOrderId(request.orderId)
            ?: throw IllegalArgumentException("Payment not found for order: ${request.orderId}")
        
        return payment.toGrpcResponse()
    }
}

private fun com.vincenzo.shopping.payment.domain.Payment.toGrpcResponse(): PaymentResponse {
    return PaymentResponse.newBuilder()
        .setId(this.id ?: 0)
        .setOrderId(this.orderId)
        .setTotalAmount(this.totalAmount)
        .setStatus(this.status.name)
        .addAllPaymentDetails(this.paymentDetails.map { detail ->
            PaymentDetailResponse.newBuilder()
                .setId(detail.id ?: 0)
                .setMethod(detail.method.name)
                .setAmount(detail.amount)
                .setTransactionId(detail.transactionId ?: "")
                .setStatus(detail.status.name)
                .putAllMetadata(detail.metadata)
                .build()
        })
        .setCreatedAt(this.createdAt.toString())
        .setCompletedAt(this.completedAt?.toString() ?: "")
        .build()
}