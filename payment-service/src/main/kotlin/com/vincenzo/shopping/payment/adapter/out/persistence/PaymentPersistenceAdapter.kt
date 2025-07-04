package com.vincenzo.shopping.payment.adapter.out.persistence

import com.vincenzo.shopping.payment.application.port.out.PaymentRepository
import com.vincenzo.shopping.payment.domain.Payment
import com.vincenzo.shopping.payment.domain.PaymentDetail
import com.vincenzo.shopping.payment.domain.PaymentDetailStatus
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
            memberId = payment.memberId,
            totalAmount = payment.totalAmount,
            paymentMethod = payment.paymentMethod,
            status = payment.status,
            createdAt = payment.createdAt,
            completedAt = payment.completedAt
        )
        val savedPayment = paymentJpaRepository.save(paymentEntity)
        
        // PaymentDetail 엔티티 생성 및 저장
        val detailEntities = payment.paymentDetails.map { detail ->
            PaymentDetailEntity(
                payment = savedPayment,
                method = detail.method,
                amount = detail.amount,
                status = detail.status,
                transactionId = detail.transactionId,
                metadata = detail.metadata.mapValues { it.value.toString() }.toMutableMap()
            )
        }
        savedPayment.paymentDetails.addAll(detailEntities)
        paymentJpaRepository.save(savedPayment)
        
        return savedPayment.toDomain()
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

private fun PaymentEntity.toDomain(): Payment {
    return Payment(
        id = this.id,
        orderId = this.orderId,
        memberId = this.memberId,
        totalAmount = this.totalAmount,
        paymentMethod = this.paymentMethod,
        status = this.status,
        paymentDetails = this.paymentDetails.map { it.toDomain() },
        createdAt = this.createdAt,
        completedAt = this.completedAt
    )
}

private fun PaymentDetailEntity.toDomain(): PaymentDetail {
    return PaymentDetail(
        method = this.method,
        amount = this.amount,
        transactionId = this.transactionId,
        status = this.status,
        metadata = this.metadata.toMap()
    )
}
