package com.vincenzo.shopping.payment.adapter.out.persistence

import com.vincenzo.shopping.payment.domain.PaymentDetailStatus
import com.vincenzo.shopping.payment.domain.PaymentMethod
import com.vincenzo.shopping.payment.domain.PaymentStatus
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "payments")
class PaymentEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(name = "order_id", nullable = false, unique = true)
    val orderId: Long,
    
    @Column(name = "total_amount", nullable = false)
    val totalAmount: Long,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: PaymentStatus,
    
    @OneToMany(mappedBy = "payment", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val paymentDetails: MutableList<PaymentDetailEntity> = mutableListOf(),
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "completed_at")
    val completedAt: LocalDateTime? = null
)

@Entity
@Table(name = "payment_details")
class PaymentDetailEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    var payment: PaymentEntity? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val method: PaymentMethod,
    
    @Column(nullable = false)
    val amount: Long,
    
    @Column(name = "transaction_id")
    val transactionId: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: PaymentDetailStatus,
    
    @ElementCollection
    @CollectionTable(
        name = "payment_detail_metadata",
        joinColumns = [JoinColumn(name = "payment_detail_id")]
    )
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    val metadata: MutableMap<String, String> = mutableMapOf()
)