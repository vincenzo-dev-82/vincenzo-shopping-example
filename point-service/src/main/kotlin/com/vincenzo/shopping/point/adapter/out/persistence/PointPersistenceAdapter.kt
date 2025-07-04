package com.vincenzo.shopping.point.adapter.out.persistence

import com.vincenzo.shopping.point.application.port.out.PointBalanceRepository
import com.vincenzo.shopping.point.application.port.out.PointTransactionRepository
import com.vincenzo.shopping.point.domain.PointBalance
import com.vincenzo.shopping.point.domain.PointTransaction
import com.vincenzo.shopping.point.domain.TransactionType
import org.springframework.stereotype.Repository

@Repository
class PointPersistenceAdapter(
    private val pointBalanceJpaRepository: PointBalanceJpaRepository,
    private val pointTransactionJpaRepository: PointTransactionJpaRepository
) : PointBalanceRepository, PointTransactionRepository {
    
    override fun findByMemberId(memberId: Long): PointBalance? {
        return pointBalanceJpaRepository.findByMemberId(memberId)?.toDomain()
    }
    
    override fun save(pointBalance: PointBalance): PointBalance {
        val entity = PointBalanceEntity(
            id = pointBalance.id,
            memberId = pointBalance.memberId,
            balance = pointBalance.balance,
            updatedAt = pointBalance.updatedAt
        )
        val saved = pointBalanceJpaRepository.save(entity)
        return saved.toDomain()
    }
    
    override fun save(transaction: PointTransaction): PointTransaction {
        val entity = PointTransactionEntity(
            id = transaction.id,
            memberId = transaction.memberId,
            amount = transaction.amount,
            type = transaction.type.name,
            description = transaction.description,
            referenceId = transaction.referenceId,
            createdAt = transaction.createdAt
        )
        val saved = pointTransactionJpaRepository.save(entity)
        return saved.toDomain()
    }
    
    override fun findByMemberId(memberId: Long): List<PointTransaction> {
        return pointTransactionJpaRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
            .map { it.toDomain() }
    }
}

private fun PointBalanceEntity.toDomain(): PointBalance {
    return PointBalance(
        id = this.id,
        memberId = this.memberId,
        balance = this.balance,
        updatedAt = this.updatedAt
    )
}

private fun PointTransactionEntity.toDomain(): PointTransaction {
    return PointTransaction(
        id = this.id,
        memberId = this.memberId,
        amount = this.amount,
        type = TransactionType.valueOf(this.type),
        description = this.description,
        referenceId = this.referenceId,
        createdAt = this.createdAt
    )
}