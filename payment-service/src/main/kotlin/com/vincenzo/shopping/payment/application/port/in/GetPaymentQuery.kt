package com.vincenzo.shopping.payment.application.port.`in`

import com.vincenzo.shopping.payment.domain.Payment

interface GetPaymentQuery {
    fun getPaymentById(paymentId: Long): Payment?
    fun getPaymentByOrderId(orderId: Long): Payment?
}
