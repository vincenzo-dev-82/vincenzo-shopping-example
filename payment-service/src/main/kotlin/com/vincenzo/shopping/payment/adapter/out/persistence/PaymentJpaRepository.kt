package com.vincenzo.shopping.payment.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface PaymentJpaRepository : JpaRepository<PaymentEntity, Long> {
    fun findByOrderId(orderId: Long): PaymentEntity?
}

interface PaymentDetailJpaRepository : JpaRepository<PaymentDetailEntity, Long> {
    fun findByPaymentId(paymentId: Long): List<PaymentDetailEntity>
}