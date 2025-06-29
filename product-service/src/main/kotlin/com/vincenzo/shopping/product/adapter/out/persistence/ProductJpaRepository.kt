package com.vincenzo.shopping.product.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface ProductJpaRepository : JpaRepository<ProductEntity, Long> {
    fun findBySellerId(sellerId: String): List<ProductEntity>
    fun findByNameContaining(name: String): List<ProductEntity>
}
