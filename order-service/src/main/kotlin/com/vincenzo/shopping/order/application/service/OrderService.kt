package com.vincenzo.shopping.order.application.service

import com.vincenzo.shopping.common.kafka.KafkaMessage
import com.vincenzo.shopping.common.kafka.KafkaTopics
import com.vincenzo.shopping.order.adapter.out.grpc.MemberServiceGrpcClient
import com.vincenzo.shopping.order.adapter.out.grpc.ProductServiceGrpcClient
import com.vincenzo.shopping.order.domain.Order
import com.vincenzo.shopping.order.domain.OrderItem
import com.vincenzo.shopping.order.domain.OrderStatus
import com.vincenzo.shopping.order.domain.PaymentMethod
import kotlinx.coroutines.runBlocking
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OrderService(
    private val memberServiceGrpcClient: MemberServiceGrpcClient,
    private val productServiceGrpcClient: ProductServiceGrpcClient,
    private val kafkaTemplate: KafkaTemplate<String, KafkaMessage<*>>
) {
    
    fun createOrder(request: CreateOrderRequest): Order = runBlocking {
        // 1. gRPC로 회원 정보 조회
        val member = memberServiceGrpcClient.getMember(request.memberId)
            ?: throw IllegalArgumentException("회원을 찾을 수 없습니다: ${request.memberId}")
        
        println("회원 정보 조회 성공: ${member.name}, 보유 포인트: ${member.point}")
        
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
        
        // 3. 결제 방법 검증
        validatePaymentMethod(request.paymentMethod, totalAmount, member.point)
        
        // 4. 주문 생성
        val order = Order(
            id = System.currentTimeMillis(), // 임시 ID
            memberId = request.memberId,
            orderItems = orderItems,
            totalAmount = totalAmount,
            status = OrderStatus.PENDING,
            paymentMethod = request.paymentMethod
        )
        
        // 5. 재고 차감 (gRPC 호출)
        for (item in request.items) {
            try {
                productServiceGrpcClient.updateStock(item.productId, -item.quantity)
                println("재고 차감 성공: 상품ID ${item.productId}, 수량 -${item.quantity}")
            } catch (e: Exception) {
                println("재고 차감 실패: ${e.message}")
                throw e
            }
        }
        
        // 6. Kafka로 주문 이벤트 발행
        val orderEvent = OrderCreatedEvent(
            orderId = order.id!!,
            memberId = order.memberId,
            totalAmount = order.totalAmount,
            paymentMethod = order.paymentMethod.name
        )
        
        kafkaTemplate.send(
            KafkaTopics.ORDER_EVENTS,
            KafkaMessage(
                eventType = "ORDER_CREATED",
                payload = orderEvent
            )
        )
        
        println("주문 생성 완료: ${order.id}")
        order
    }
    
    private fun validatePaymentMethod(
        paymentMethod: PaymentMethod,
        totalAmount: Long,
        memberPoint: Int
    ) {
        when (paymentMethod) {
            PaymentMethod.CASHNOTE_POINT -> {
                if (memberPoint < totalAmount) {
                    throw IllegalArgumentException("캐시노트 포인트가 부족합니다. 필요: $totalAmount, 보유: $memberPoint")
                }
            }
            PaymentMethod.BNPL -> {
                // BNPL은 단독 결제만 가능
                println("BNPL 결제 검증 통과")
            }
            else -> {
                // PG, COMPOSITE 등
                println("결제 방법 검증 통과: $paymentMethod")
            }
        }
    }
}

data class CreateOrderRequest(
    val memberId: Long,
    val items: List<OrderItemRequest>,
    val paymentMethod: PaymentMethod
)

data class OrderItemRequest(
    val productId: Long,
    val quantity: Int
)

data class OrderCreatedEvent(
    val orderId: Long,
    val memberId: Long,
    val totalAmount: Long,
    val paymentMethod: String
)
