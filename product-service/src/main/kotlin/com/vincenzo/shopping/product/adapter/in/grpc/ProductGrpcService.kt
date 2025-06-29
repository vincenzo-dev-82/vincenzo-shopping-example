package com.vincenzo.shopping.product.adapter.`in`.grpc

import com.vincenzo.shopping.grpc.product.*
import com.vincenzo.shopping.product.domain.Product
import net.devh.boot.grpc.server.service.GrpcService
import org.springframework.stereotype.Component

@GrpcService
class ProductGrpcService : ProductServiceGrpcKt.ProductServiceCoroutineImplBase() {
    
    // 임시 데이터 저장소 (실제로는 서비스와 리포지토리를 통해 구현)
    private val products = mutableMapOf(
        1L to Product(1, "캐시노트 포인트 충전권 1000원", 1000, 100, "cashnote"),
        2L to Product(2, "캐시노트 포인트 충전권 5000원", 5000, 50, "cashnote"),
        3L to Product(3, "캐시노트 포인트 충전권 10000원", 10000, 30, "cashnote")
    )
    
    override suspend fun getProduct(request: GetProductRequest): ProductResponse {
        val product = products[request.productId]
            ?: throw IllegalArgumentException("Product not found: ${request.productId}")
        
        return product.toGrpcResponse()
    }
    
    override suspend fun getProductList(request: GetProductListRequest): ProductListResponse {
        val productList = request.productIdsList.mapNotNull { products[it] }
        
        return ProductListResponse.newBuilder()
            .addAllProducts(productList.map { it.toGrpcResponse() })
            .build()
    }
    
    override suspend fun updateStock(request: UpdateStockRequest): ProductResponse {
        val product = products[request.productId]
            ?: throw IllegalArgumentException("Product not found: ${request.productId}")
        
        val updatedProduct = product.copy(stock = product.stock + request.quantityChange)
        products[request.productId] = updatedProduct
        
        return updatedProduct.toGrpcResponse()
    }
}

private fun Product.toGrpcResponse(): ProductResponse {
    return ProductResponse.newBuilder()
        .setId(this.id ?: 0)
        .setName(this.name)
        .setPrice(this.price)
        .setStock(this.stock)
        .setSellerId(this.sellerId)
        .build()
}
