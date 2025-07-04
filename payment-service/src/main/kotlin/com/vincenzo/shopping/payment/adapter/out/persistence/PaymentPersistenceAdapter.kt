package com.vincenzo.shopping.payment.adapter.out.persistence

import com.vincenzo.shopping.payment.application.port.out.PaymentRepository
import com.vincenzo.shopping.payment.domain.Payment
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
            status = payment.status.name,
            createdAt = payment.createdAt,
            completedAt = payment.completedAt
        )
        val savedPayment = paymentJpaRepository.save(paymentEntity)
        
        // PaymentDetail 엔티티 생성 및 저장
        val detailEntities = payment.paymentDetails.map { detail ->
            PaymentDetailEntity(
                id = detail.id,
                paymentId = savedPayment.id!!,
                method = detail.method.name,
                amount = detail.amount,
                status = detail.status.name,
                transactionId = detail.transactionId,
                metadata = detail.metadata
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