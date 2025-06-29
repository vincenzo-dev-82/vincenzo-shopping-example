package com.vincenzo.shopping.product.adapter.`in`.web

import com.vincenzo.shopping.product.application.port.`in`.CreateProductCommand
import com.vincenzo.shopping.product.application.port.`in`.CreateProductUseCase
import com.vincenzo.shopping.product.application.port.`in`.GetProductQuery
import com.vincenzo.shopping.product.application.port.`in`.UpdateProductStockUseCase
import com.vincenzo.shopping.product.domain.Product
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val createProductUseCase: CreateProductUseCase,
    private val getProductQuery: GetProductQuery,
    private val updateProductStockUseCase: UpdateProductStockUseCase
) {
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createProduct(@RequestBody request: CreateProductRequest): ProductResponse {
        val command = CreateProductCommand(
            name = request.name,
            price = request.price,
            stock = request.stock,
            sellerId = request.sellerId
        )
        val product = createProductUseCase.createProduct(command)
        return ProductResponse.from(product)
    }
    
    @GetMapping("/{productId}")
    fun getProduct(@PathVariable productId: Long): ProductResponse {
        val product = getProductQuery.getProduct(productId)
            ?: throw NoSuchElementException("Product not found with id: $productId")
        return ProductResponse.from(product)
    }
    
    @GetMapping
    fun getAllProducts(): List<ProductResponse> {
        return getProductQuery.getAllProducts()
            .map { ProductResponse.from(it) }
    }
    
    @GetMapping("/seller/{sellerId}")
    fun getProductsBySeller(@PathVariable sellerId: String): List<ProductResponse> {
        return getProductQuery.getProductsBySeller(sellerId)
            .map { ProductResponse.from(it) }
    }
    
    @PatchMapping("/{productId}/stock")
    fun updateStock(
        @PathVariable productId: Long,
        @RequestBody request: UpdateStockRequest
    ): ProductResponse {
        val product = updateProductStockUseCase.updateStock(productId, request.quantityChange)
        return ProductResponse.from(product)
    }
    
    @GetMapping("/hello")
    fun hello(): String {
        return "Hello from Product Service!"
    }
}

data class CreateProductRequest(
    val name: String,
    val price: Long,
    val stock: Int,
    val sellerId: String
)

data class UpdateStockRequest(
    val quantityChange: Int
)

data class ProductResponse(
    val id: Long?,
    val name: String,
    val price: Long,
    val stock: Int,
    val sellerId: String
) {
    companion object {
        fun from(product: Product): ProductResponse {
            return ProductResponse(
                id = product.id,
                name = product.name,
                price = product.price,
                stock = product.stock,
                sellerId = product.sellerId
            )
        }
    }
}
