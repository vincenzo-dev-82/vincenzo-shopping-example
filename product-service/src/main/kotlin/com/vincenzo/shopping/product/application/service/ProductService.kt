package com.vincenzo.shopping.product.application.service

import com.vincenzo.shopping.product.application.port.`in`.CreateProductCommand
import com.vincenzo.shopping.product.application.port.`in`.CreateProductUseCase
import com.vincenzo.shopping.product.application.port.`in`.GetProductQuery
import com.vincenzo.shopping.product.application.port.`in`.UpdateProductStockUseCase
import com.vincenzo.shopping.product.application.port.out.ProductRepository
import com.vincenzo.shopping.product.domain.Product
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProductService(
    private val productRepository: ProductRepository
) : CreateProductUseCase, GetProductQuery, UpdateProductStockUseCase {
    
    override fun createProduct(command: CreateProductCommand): Product {
        val product = Product(
            name = command.name,
            price = command.price,
            stock = command.stock,
            sellerId = command.sellerId
        )
        return productRepository.save(product)
    }
    
    @Transactional(readOnly = true)
    override fun getProduct(productId: Long): Product? {
        return productRepository.findById(productId)
    }
    
    @Transactional(readOnly = true)
    override fun getAllProducts(): List<Product> {
        return productRepository.findAll()
    }
    
    @Transactional(readOnly = true)
    override fun getProductsBySeller(sellerId: String): List<Product> {
        return productRepository.findBySellerId(sellerId)
    }
    
    override fun updateStock(productId: Long, quantityChange: Int): Product {
        val product = productRepository.findById(productId)
            ?: throw IllegalArgumentException("Product not found: $productId")
        
        val newStock = product.stock + quantityChange
        if (newStock < 0) {
            throw IllegalStateException("Insufficient stock. Current: ${product.stock}, Requested change: $quantityChange")
        }
        
        val updatedProduct = product.copy(stock = newStock)
        return productRepository.save(updatedProduct)
    }
}
