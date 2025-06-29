package com.vincenzo.shopping.product.application.port.`in`

import com.vincenzo.shopping.product.domain.Product

interface GetProductQuery {
    fun getProduct(productId: Long): Product?
    fun getAllProducts(): List<Product>
    fun getProductsBySeller(sellerId: String): List<Product>
}
