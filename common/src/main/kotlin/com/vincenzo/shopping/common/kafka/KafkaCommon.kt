package com.vincenzo.shopping.common.kafka

data class KafkaMessage<T>(
    val eventType: String,
    val payload: T,
    val timestamp: Long = System.currentTimeMillis()
)

object KafkaTopics {
    const val ORDER_EVENTS = "order-events"
    const val PAYMENT_EVENTS = "payment-events"
}
