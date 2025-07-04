package com.vincenzo.shopping.payment.adapter.out.persistence

import com.vincenzo.shopping.payment.application.port.out.PaymentRepository
import com.vincenzo.shopping.payment.domain.Payment
import com.vincenzo.shopping.payment.domain.PaymentDetail
import org.springframework.stereotype.Repository

@Repository
class PaymentPersistenceAdapter(
    private val paymentJpaRepository: PaymentJpaRepository,
    private val paymentDetailJpaRepository: PaymentDetailJpaRepository
) : PaymentRepository {
    
    override fun save(payment: Payment): Payment {
        // Payment 엔티티 생성 및 저장
        val paymentEntity = PaymentEntity(
            id = payment.id,
            orderId = payment.orderId,
            totalAmount = payment.totalAmount,
            status = payment.status,
            createdAt = payment.createdAt,
            completedAt = payment.completedAt
        )
        val savedPayment = paymentJpaRepository.save(paymentEntity)
        
        // PaymentDetail 엔티티 생성 및 저장
        val detailEntities = payment.paymentDetails.map { detail ->
            PaymentDetailEntity(
                id = detail.id,
                payment = savedPayment,
                method = detail.method,
                amount = detail.amount,
                status = detail.status,
                transactionId = detail.transactionId,
                metadata = detail.metadata.toMutableMap()
            )
        }
        val savedDetails = paymentDetailJpaRepository.saveAll(detailEntities)
        
        return savedPayment.toDomain(savedDetails)
    }
    
    override fun findById(id: Long): Payment? {
        val paymentEntity = paymentJpaRepository.findById(id).orElse(null) ?: return null
        val detailEntities = paymentDetailJpaRepository.findByPaymentId(id)
        return paymentEntity.toDomain(detailEntities)
    }
    
    override fun findByOrderId(orderId: Long): Payment? {
        val paymentEntity = paymentJpaRepository.findByOrderId(orderId) ?: return null
        val detailEntities = paymentDetailJpaRepository.findByPaymentId(paymentEntity.id!!)
        return paymentEntity.toDomain(detailEntities)
    }
}

private fun PaymentEntity.toDomain(details: List<PaymentDetailEntity>): Payment {
    return Payment(
        id = this.id,
        orderId = this.orderId,
        paymentDetails = details.map { it.toDomain() },
        totalAmount = this.totalAmount,
        status = this.status,
        createdAt = this.createdAt,
        completedAt = this.completedAt
    )
}

private fun PaymentDetailEntity.toDomain(): PaymentDetail {
    return PaymentDetail(
        id = this.id,
        paymentId = this.payment?.id,
        method = this.method,
        amount = this.amount,
        status = this.status,
        transactionId = this.transactionId,
        metadata = this.metadata.toMap()
    )
}