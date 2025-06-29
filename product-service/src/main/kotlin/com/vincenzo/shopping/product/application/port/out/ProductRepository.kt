package com.vincenzo.shopping.product.application.port.out

import com.vincenzo.shopping.product.domain.Product

interface ProductRepository {
    fun save(product: Product): Product
    fun findById(id: Long): Product?
    fun findAll(): List<Product>
    fun findBySellerId(sellerId: String): List<Product>
    fun deleteById(id: Long)
}
