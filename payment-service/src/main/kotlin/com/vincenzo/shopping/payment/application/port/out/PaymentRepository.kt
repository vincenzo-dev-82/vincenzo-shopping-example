package com.vincenzo.shopping.payment.application.port.out

import com.vincenzo.shopping.payment.domain.Payment

interface PaymentRepository {
    fun save(payment: Payment): Payment
    fun findById(id: Long): Payment?
    fun findByOrderId(orderId: Long): Payment?
}
