package com.vincenzo.shopping.product.config

import com.vincenzo.shopping.product.application.port.`in`.CreateProductCommand
import com.vincenzo.shopping.product.application.port.`in`.CreateProductUseCase
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataInitializer {
    
    @Bean
    fun initData(createProductUseCase: CreateProductUseCase) = CommandLineRunner {
        // 샘플 상품 데이터 생성
        val products = listOf(
            CreateProductCommand(
                name = "캐시노트 포인트 충전권 1000원",
                price = 1000,
                stock = 100,
                sellerId = "cashnote"
            ),
            CreateProductCommand(
                name = "캐시노트 포인트 충전권 5000원",
                price = 5000,
                stock = 50,
                sellerId = "cashnote"
            ),
            CreateProductCommand(
                name = "캐시노트 포인트 충전권 10000원",
                price = 10000,
                stock = 30,
                sellerId = "cashnote"
            ),
            CreateProductCommand(
                name = "캐시노트 포인트 충전권 50000원",
                price = 50000,
                stock = 10,
                sellerId = "cashnote"
            )
        )
        
        products.forEach { command ->
            try {
                val product = createProductUseCase.createProduct(command)
                println("Created product: ${product.name} with id: ${product.id}")
            } catch (e: Exception) {
                println("Error creating product: ${e.message}")
            }
        }
    }
}
