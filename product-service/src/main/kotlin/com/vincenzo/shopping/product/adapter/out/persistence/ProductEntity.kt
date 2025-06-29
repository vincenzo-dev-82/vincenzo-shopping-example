package com.vincenzo.shopping.product.adapter.out.persistence

import jakarta.persistence.*

@Entity
@Table(name = "products")
class ProductEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false)
    val name: String,
    
    @Column(nullable = false)
    val price: Long,
    
    @Column(nullable = false)
    val stock: Int,
    
    @Column(name = "seller_id", nullable = false)
    val sellerId: String
)
