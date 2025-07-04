package com.vincenzo.shopping.product

import com.vincenzo.shopping.product.application.port.`in`.CreateProductCommand
import com.vincenzo.shopping.product.application.port.out.ProductRepository
import com.vincenzo.shopping.product.application.service.ProductService
import com.vincenzo.shopping.product.domain.Product
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class ProductServiceTest {

    private val productRepository: ProductRepository = mock(ProductRepository::class.java)
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
        
        whenever(productRepository.save(any())).thenReturn(savedProduct)

        // when
        val result = productService.createProduct(command)

        // then
        assertEquals(savedProduct.name, result.name)
        assertEquals(savedProduct.price, result.price)
        assertEquals(savedProduct.stock, result.stock)
        assertEquals(savedProduct.sellerId, result.sellerId)
        
        verify(productRepository, times(1)).save(any())
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
        
        whenever(productRepository.findById(productId)).thenReturn(product)
        whenever(productRepository.save(any())).thenReturn(updatedProduct)

        // when
        val result = productService.updateStock(productId, quantityChange)

        // then
        assertEquals(95, result.stock)
        
        verify(productRepository, times(1)).findById(productId)
        verify(productRepository, times(1)).save(any())
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
        
        whenever(productRepository.findById(productId)).thenReturn(product)

        // when & then
        assertThrows<IllegalStateException> {
            productService.updateStock(productId, quantityChange)
        }
        
        verify(productRepository, times(1)).findById(productId)
        verify(productRepository, never()).save(any())
    }
}
