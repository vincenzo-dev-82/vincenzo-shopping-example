package com.vincenzo.shopping.point.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface PointBalanceJpaRepository : JpaRepository<PointBalanceEntity, Long> {
    fun findByMemberId(memberId: Long): PointBalanceEntity?
}

interface PointTransactionJpaRepository : JpaRepository<PointTransactionEntity, Long> {
    fun findByMemberIdOrderByCreatedAtDesc(memberId: Long): List<PointTransactionEntity>
    
    @Query("SELECT SUM(t.amount) FROM PointTransactionEntity t WHERE t.memberId = :memberId")
    fun calculateBalance(memberId: Long): Int?
}