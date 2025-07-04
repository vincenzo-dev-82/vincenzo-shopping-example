package com.vincenzo.shopping.order.application.service

import com.vincenzo.shopping.common.kafka.KafkaMessage
import com.vincenzo.shopping.common.kafka.KafkaTopics
import com.vincenzo.shopping.order.adapter.out.grpc.*
import com.vincenzo.shopping.order.domain.*
import kotlinx.coroutines.runBlocking
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OrderService(
    private val memberServiceGrpcClient: MemberServiceGrpcClient,
    private val productServiceGrpcClient: ProductServiceGrpcClient,
    private val pointServiceGrpcClient: PointServiceGrpcClient,
    private val paymentServiceGrpcClient: PaymentServiceGrpcClient,
    private val kafkaTemplate: KafkaTemplate<String, KafkaMessage<*>>
) {
    
    fun createOrder(request: CreateOrderRequest): Order = runBlocking {
        // 1. gRPC로 회원 정보 조회
        val member = memberServiceGrpcClient.getMember(request.memberId)
            ?: throw IllegalArgumentException("회원을 찾을 수 없습니다: ${request.memberId}")
        
        println("회원 정보 조회 성공: ${member.name}")
        
        // 2. gRPC로 상품 정보 조회 및 재고 확인
        val orderItems = mutableListOf<OrderItem>()
        var totalAmount = 0L
        
        for (item in request.items) {
            val product = productServiceGrpcClient.getProduct(item.productId)
                ?: throw IllegalArgumentException("상품을 찾을 수 없습니다: ${item.productId}")
            
            println("상품 정보 조회 성공: ${product.name}, 재고: ${product.stock}")
            
            // 재고 확인
            if (product.stock < item.quantity) {
                throw IllegalStateException("재고가 부족합니다. 상품: ${product.name}, 재고: ${product.stock}, 요청수량: ${item.quantity}")
            }
            
            orderItems.add(
                OrderItem(
                    productId = product.id,
                    productName = product.name,
                    price = product.price,
                    quantity = item.quantity
                )
            )
            
            totalAmount += product.price * item.quantity
        }
        
        // 3. 결제 방법에 따른 결제 상세 정보 생성
        val paymentDetails = when (request.paymentMethod) {
            PaymentMethod.PG_KPN -> {
                // PG 단독 결제
                listOf(OrderPaymentDetail(
                    method = PaymentMethod.PG_KPN,
                    amount = totalAmount
                ))
            }
            PaymentMethod.POINT -> {
                // 포인트 단독 결제 - 잔액 확인
                val pointBalance = pointServiceGrpcClient.getBalance(request.memberId)
                    ?: throw IllegalStateException("포인트 잔액 조회 실패")
                
                if (pointBalance.balance < totalAmount) {
                    throw IllegalArgumentException("포인트가 부족합니다. 필요: $totalAmount, 보유: ${pointBalance.balance}")
                }
                
                listOf(OrderPaymentDetail(
                    method = PaymentMethod.POINT,
                    amount = totalAmount
                ))
            }
            PaymentMethod.BNPL -> {
                // BNPL 단독 결제
                listOf(OrderPaymentDetail(
                    method = PaymentMethod.BNPL,
                    amount = totalAmount
                ))
            }
            else -> {
                // 복합결제의 경우 별도 처리 필요
                request.paymentDetails ?: throw IllegalArgumentException("복합결제 시 결제 상세 정보가 필요합니다.")
            }
        }
        
        // 4. 결제 금액 검증
        val paymentSum = paymentDetails.sumOf { it.amount }
        if (paymentSum != totalAmount) {
            throw IllegalArgumentException("결제 금액이 일치하지 않습니다. 주문금액: $totalAmount, 결제금액: $paymentSum")
        }
        
        // 5. 주문 생성
        val order = Order(
            id = System.currentTimeMillis(), // 임시 ID
            memberId = request.memberId,
            orderItems = orderItems,
            totalAmount = totalAmount,
            status = OrderStatus.PENDING,
            paymentMethod = request.paymentMethod,
            paymentDetails = paymentDetails
        )
        
        // 6. 재고 차감 (gRPC 호출)
        for (item in request.items) {
            try {
                productServiceGrpcClient.updateStock(item.productId, -item.quantity)
                println("재고 차감 성공: 상품ID ${item.productId}, 수량 -${item.quantity}")
            } catch (e: Exception) {
                println("재고 차감 실패: ${e.message}")
                // TODO: 재고 롤백 처리
                throw e
            }
        }
        
        // 7. 결제 처리 (gRPC 호출)
        val paymentResult = try {
            val paymentDetailInfos = paymentDetails.map { detail ->
                PaymentDetailInfo(
                    method = detail.method.name,
                    amount = detail.amount,
                    metadata = detail.metadata
                )
            }
            
            paymentServiceGrpcClient.processPayment(
                orderId = order.id!!,
                memberId = order.memberId,
                totalAmount = order.totalAmount,
                paymentDetails = paymentDetailInfos
            )
        } catch (e: Exception) {
            println("결제 처리 실패: ${e.message}")
            // TODO: 재고 롤백 처리
            throw e
        }
        
        // 8. 주문 상태 업데이트
        val updatedOrder = if (paymentResult != null && paymentResult.status == "COMPLETED") {
            order.copy(status = OrderStatus.PAID)
        } else {
            order.copy(status = OrderStatus.PAYMENT_FAILED)
        }
        
        // 9. Kafka로 주문 이벤트 발행
        val orderEvent = OrderCreatedEvent(
            orderId = updatedOrder.id!!,
            memberId = updatedOrder.memberId,
            totalAmount = updatedOrder.totalAmount,
            paymentMethod = updatedOrder.paymentMethod.name,
            status = updatedOrder.status.name
        )
        
        kafkaTemplate.send(
            KafkaTopics.ORDER_EVENTS,
            KafkaMessage(
                eventType = "ORDER_CREATED",
                payload = orderEvent
            )
        )
        
        println("주문 생성 완료: ${updatedOrder.id}, 상태: ${updatedOrder.status}")
        updatedOrder
    }
}

data class CreateOrderRequest(
    val memberId: Long,
    val items: List<OrderItemRequest>,
    val paymentMethod: PaymentMethod,
    val paymentDetails: List<OrderPaymentDetail>? = null  // 복합결제 시 필요
)

data class OrderItemRequest(
    val productId: Long,
    val quantity: Int
)

data class OrderCreatedEvent(
    val orderId: Long,
    val memberId: Long,
    val totalAmount: Long,
    val paymentMethod: String,
    val status: String
)