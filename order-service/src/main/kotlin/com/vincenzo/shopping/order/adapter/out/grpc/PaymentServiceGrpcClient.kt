package com.vincenzo.shopping.order.adapter.out.grpc

import com.vincenzo.shopping.grpc.payment.*
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component

@Component
class PaymentServiceGrpcClient {
    
    @GrpcClient("payment-service")
    private lateinit var paymentServiceStub: PaymentServiceGrpcKt.PaymentServiceCoroutineStub
    
    suspend fun processPayment(
        orderId: Long,
        memberId: Long,
        totalAmount: Long,
        paymentDetails: List<PaymentDetailInfo>
    ): PaymentInfo? {
        return try {
            val request = ProcessPaymentRequest.newBuilder()
                .setOrderId(orderId)
                .setMemberId(memberId)
                .setTotalAmount(totalAmount)
                .addAllPaymentDetails(paymentDetails.map { detail ->
                    PaymentDetailRequest.newBuilder()
                        .setMethod(detail.method)
                        .setAmount(detail.amount)
                        .putAllMetadata(detail.metadata)
                        .build()
                })
                .build()
            
            val response = paymentServiceStub.processPayment(request)
            
            PaymentInfo(
                id = response.id,
                orderId = response.orderId,
                totalAmount = response.totalAmount,
                status = response.status,
                paymentDetails = response.paymentDetailsList.map { detail ->
                    PaymentDetailResult(
                        id = detail.id,
                        method = detail.method,
                        amount = detail.amount,
                        transactionId = detail.transactionId,
                        status = detail.status,
                        metadata = detail.metadataMap
                    )
                },
                createdAt = response.createdAt,
                completedAt = response.completedAt.takeIf { it.isNotEmpty() }
            )
        } catch (e: Exception) {
            println("결제 처리 실패: ${e.message}")
            null
        }
    }
}

data class PaymentDetailInfo(
    val method: String,
    val amount: Long,
    val metadata: Map<String, String> = emptyMap()
)

data class PaymentInfo(
    val id: Long,
    val orderId: Long,
    val totalAmount: Long,
    val status: String,
    val paymentDetails: List<PaymentDetailResult>,
    val createdAt: String,
    val completedAt: String?
)

data class PaymentDetailResult(
    val id: Long,
    val method: String,
    val amount: Long,
    val transactionId: String,
    val status: String,
    val metadata: Map<String, String>
)