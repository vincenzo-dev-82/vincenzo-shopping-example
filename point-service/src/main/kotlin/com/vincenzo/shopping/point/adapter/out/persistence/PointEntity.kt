package com.vincenzo.shopping.point.adapter.out.persistence

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "point_balances")
class PointBalanceEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(name = "member_id", nullable = false, unique = true)
    val memberId: Long,
    
    @Column(nullable = false)
    val balance: Int = 0,
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity
@Table(name = "point_transactions")
class PointTransactionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(name = "member_id", nullable = false)
    val memberId: Long,
    
    @Column(nullable = false)
    val amount: Int,
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val type: String,
    
    @Column(nullable = false)
    val description: String,
    
    @Column(name = "reference_id")
    val referenceId: String? = null,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)