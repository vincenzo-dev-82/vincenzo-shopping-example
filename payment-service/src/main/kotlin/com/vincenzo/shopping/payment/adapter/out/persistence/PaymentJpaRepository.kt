package com.vincenzo.shopping.payment.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface PaymentJpaRepository : JpaRepository<PaymentEntity, Long> {
    fun findByOrderId(orderId: Long): PaymentEntity?
}