package com.vincenzo.shopping.product.domain

data class Product(
    val id: Long? = null,
    val name: String,
    val price: Long,
    val stock: Int,
    val sellerId: String
)
