package com.vincenzo.shopping.payment.adapter.out.persistence

import com.vincenzo.shopping.payment.application.port.out.PaymentRepository
import com.vincenzo.shopping.payment.domain.Payment
import com.vincenzo.shopping.payment.domain.PaymentDetail
import org.springframework.stereotype.Repository

@Repository
class PaymentPersistenceAdapter(
    private val paymentJpaRepository: PaymentJpaRepository
) : PaymentRepository {
    
    override fun save(payment: Payment): Payment {
        val entity = payment.toEntity()
        
        // 양방향 관계 설정
        entity.paymentDetails.forEach { detail ->
            detail.payment = entity
        }
        
        val savedEntity = paymentJpaRepository.save(entity)
        return savedEntity.toDomain()
    }
    
    override fun findById(id: Long): Payment? {
        return paymentJpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }
    
    override fun findByOrderId(orderId: Long): Payment? {
        return paymentJpaRepository.findByOrderId(orderId)?.toDomain()
    }
}

private fun Payment.toEntity(): PaymentEntity {
    val entity = PaymentEntity(
        id = this.id,
        orderId = this.orderId,
        totalAmount = this.totalAmount,
        status = this.status,
        createdAt = this.createdAt,
        completedAt = this.completedAt
    )
    
    val detailEntities = this.paymentDetails.map { detail ->
        PaymentDetailEntity(
            id = detail.id,
            payment = entity,
            method = detail.method,
            amount = detail.amount,
            transactionId = detail.transactionId,
            status = detail.status,
            metadata = detail.metadata.toMutableMap()
        )
    }
    
    entity.paymentDetails.clear()
    entity.paymentDetails.addAll(detailEntities)
    
    return entity
}

private fun PaymentEntity.toDomain(): Payment {
    return Payment(
        id = this.id,
        orderId = this.orderId,
        totalAmount = this.totalAmount,
        status = this.status,
        paymentDetails = this.paymentDetails.map { it.toDomain() },
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
        transactionId = this.transactionId,
        status = this.status,
        metadata = this.metadata.toMap()
    )
}