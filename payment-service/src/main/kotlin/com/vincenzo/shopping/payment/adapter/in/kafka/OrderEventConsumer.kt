package com.vincenzo.shopping.payment.adapter.`in`.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.vincenzo.shopping.common.kafka.KafkaMessage
import com.vincenzo.shopping.common.kafka.KafkaTopics
import com.vincenzo.shopping.payment.application.port.`in`.PaymentDetailCommand
import com.vincenzo.shopping.payment.application.port.`in`.ProcessPaymentCommand
import com.vincenzo.shopping.payment.application.port.`in`.ProcessPaymentUseCase
import com.vincenzo.shopping.payment.domain.PaymentMethod
import kotlinx.coroutines.runBlocking
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class OrderEventConsumer(
    private val processPaymentUseCase: ProcessPaymentUseCase,
    private val objectMapper: ObjectMapper
) {
    
    @KafkaListener(
        topics = [KafkaTopics.ORDER_EVENTS],
        groupId = "payment-service"
    )
    fun handleOrderEvent(message: String) = runBlocking {
        println("[Payment-Kafka] 주문 이벤트 수신: $message")
        
        try {
            val kafkaMessage: KafkaMessage<Map<String, Any>> = objectMapper.readValue(message)
            
            when (kafkaMessage.eventType) {
                "ORDER_CREATED" -> handleOrderCreated(kafkaMessage.payload)
                else -> println("[Payment-Kafka] 처리하지 않는 이벤트 타입: ${kafkaMessage.eventType}")
            }
        } catch (e: Exception) {
            println("[Payment-Kafka] 이벤트 처리 오류: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private suspend fun handleOrderCreated(payload: Map<String, Any>) {
        val orderId = (payload["orderId"] as Number).toLong()
        val memberId = (payload["memberId"] as Number).toLong()
        val totalAmount = (payload["totalAmount"] as Number).toLong()
        val paymentMethodStr = payload["paymentMethod"] as String
        
        println("[Payment-Kafka] 주문 생성 이벤트 처리 - 주문: $orderId, 금액: $totalAmount, 방법: $paymentMethodStr")
        
        // 결제 방법에 따른 상세 정보 구성
        val paymentDetails = when (paymentMethodStr) {
            "COMPOSITE" -> {
                // 복합결제 예시 (실제로는 주문 서비스에서 더 상세한 정보를 전달해야 함)
                listOf(
                    PaymentDetailCommand(
                        method = PaymentMethod.PG_KPN,
                        amount = totalAmount * 70 / 100, // 70%는 PG
                        metadata = mapOf("cardNumber" to "1234-5678-9012-3456")
                    ),
                    PaymentDetailCommand(
                        method = PaymentMethod.CASHNOTE_POINT,
                        amount = totalAmount * 20 / 100, // 20%는 포인트
                        metadata = emptyMap()
                    ),
                    PaymentDetailCommand(
                        method = PaymentMethod.COUPON,
                        amount = totalAmount * 10 / 100, // 10%는 쿠폰
                        metadata = mapOf("couponCode" to "WELCOME10")
                    )
                )
            }
            "BNPL" -> {
                listOf(
                    PaymentDetailCommand(
                        method = PaymentMethod.BNPL,
                        amount = totalAmount,
                        metadata = mapOf("installmentMonths" to "3")
                    )
                )
            }
            else -> {
                listOf(
                    PaymentDetailCommand(
                        method = PaymentMethod.valueOf(paymentMethodStr),
                        amount = totalAmount,
                        metadata = emptyMap()
                    )
                )
            }
        }
        
        val command = ProcessPaymentCommand(
            orderId = orderId,
            memberId = memberId,
            totalAmount = totalAmount,
            paymentDetails = paymentDetails
        )
        
        try {
            val payment = processPaymentUseCase.processPayment(command)
            println("[Payment-Kafka] 결제 처리 완료 - 상태: ${payment.status}")
        } catch (e: Exception) {
            println("[Payment-Kafka] 결제 처리 실패: ${e.message}")
        }
    }
}