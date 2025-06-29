package com.vincenzo.shopping.product.application.port.`in`

import com.vincenzo.shopping.product.domain.Product

interface UpdateProductStockUseCase {
    fun updateStock(productId: Long, quantityChange: Int): Product
}
