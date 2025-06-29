package com.vincenzo.shopping.product.application.port.`in`

import com.vincenzo.shopping.product.domain.Product

interface CreateProductUseCase {
    fun createProduct(command: CreateProductCommand): Product
}

data class CreateProductCommand(
    val name: String,
    val price: Long,
    val stock: Int,
    val sellerId: String
)
