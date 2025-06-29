package com.vincenzo.shopping.product.adapter.out.persistence

import com.vincenzo.shopping.product.application.port.out.ProductRepository
import com.vincenzo.shopping.product.domain.Product
import org.springframework.stereotype.Repository

@Repository
class ProductPersistenceAdapter(
    private val productJpaRepository: ProductJpaRepository
) : ProductRepository {
    
    override fun save(product: Product): Product {
        val entity = ProductEntity(
            id = product.id,
            name = product.name,
            price = product.price,
            stock = product.stock,
            sellerId = product.sellerId
        )
        val savedEntity = productJpaRepository.save(entity)
        return savedEntity.toDomain()
    }
    
    override fun findById(id: Long): Product? {
        return productJpaRepository.findById(id)
            .map { it.toDomain() }
            .orElse(null)
    }
    
    override fun findAll(): List<Product> {
        return productJpaRepository.findAll()
            .map { it.toDomain() }
    }
    
    override fun findBySellerId(sellerId: String): List<Product> {
        return productJpaRepository.findBySellerId(sellerId)
            .map { it.toDomain() }
    }
    
    override fun deleteById(id: Long) {
        productJpaRepository.deleteById(id)
    }
}

private fun ProductEntity.toDomain(): Product {
    return Product(
        id = this.id,
        name = this.name,
        price = this.price,
        stock = this.stock,
        sellerId = this.sellerId
    )
}
