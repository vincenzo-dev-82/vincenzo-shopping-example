package com.vincenzo.shopping.product

import com.vincenzo.shopping.product.application.port.`in`.CreateProductCommand
import com.vincenzo.shopping.product.application.port.out.ProductRepository
import com.vincenzo.shopping.product.application.service.ProductService
import com.vincenzo.shopping.product.domain.Product
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProductServiceTest {

    private val productRepository: ProductRepository = mockk()
    private val productService = ProductService(productRepository)

    @Test
    fun `상품 생성 성공`() {
        // given
        val command = CreateProductCommand(
            name = "테스트 상품",
            price = 10000,
            stock = 100,
            sellerId = "seller001"
        )
        
        val savedProduct = Product(
            id = 1L,
            name = command.name,
            price = command.price,
            stock = command.stock,
            sellerId = command.sellerId
        )
        
        every { productRepository.save(any()) } returns savedProduct

        // when
        val result = productService.createProduct(command)

        // then
        assertEquals(savedProduct.name, result.name)
        assertEquals(savedProduct.price, result.price)
        assertEquals(savedProduct.stock, result.stock)
        assertEquals(savedProduct.sellerId, result.sellerId)
        
        verify(exactly = 1) { productRepository.save(any()) }
    }

    @Test
    fun `재고 업데이트 성공`() {
        // given
        val productId = 1L
        val quantityChange = -5
        val product = Product(
            id = productId,
            name = "테스트 상품",
            price = 10000,
            stock = 100,
            sellerId = "seller001"
        )
        
        val updatedProduct = product.copy(stock = product.stock + quantityChange)
        
        every { productRepository.findById(productId) } returns product
        every { productRepository.save(any()) } returns updatedProduct

        // when
        val result = productService.updateStock(productId, quantityChange)

        // then
        assertEquals(95, result.stock)
        
        verify(exactly = 1) { productRepository.findById(productId) }
        verify(exactly = 1) { productRepository.save(any()) }
    }

    @Test
    fun `재고 부족으로 업데이트 실패`() {
        // given
        val productId = 1L
        val quantityChange = -150  // 재고보다 많은 수량
        val product = Product(
            id = productId,
            name = "테스트 상품",
            price = 10000,
            stock = 100,
            sellerId = "seller001"
        )
        
        every { productRepository.findById(productId) } returns product

        // when & then
        assertThrows<IllegalStateException> {
            productService.updateStock(productId, quantityChange)
        }
        
        verify(exactly = 1) { productRepository.findById(productId) }
        verify(exactly = 0) { productRepository.save(any()) }
    }
}