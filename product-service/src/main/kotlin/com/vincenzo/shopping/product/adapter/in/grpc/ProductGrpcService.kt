package com.vincenzo.shopping.product.adapter.`in`.grpc

import com.vincenzo.shopping.grpc.product.*
import com.vincenzo.shopping.product.application.port.`in`.GetProductQuery
import com.vincenzo.shopping.product.application.port.`in`.UpdateProductStockUseCase
import com.vincenzo.shopping.product.domain.Product
import net.devh.boot.grpc.server.service.GrpcService

@GrpcService
class ProductGrpcService(
    private val getProductQuery: GetProductQuery,
    private val updateProductStockUseCase: UpdateProductStockUseCase
) : ProductServiceGrpcKt.ProductServiceCoroutineImplBase() {
    
    override suspend fun getProduct(request: GetProductRequest): ProductResponse {
        val product = getProductQuery.getProduct(request.productId)
            ?: throw IllegalArgumentException("Product not found: ${request.productId}")
        
        return product.toGrpcResponse()
    }
    
    override suspend fun getProductList(request: GetProductListRequest): ProductListResponse {
        val productList = request.productIdsList.mapNotNull { 
            getProductQuery.getProduct(it)
        }
        
        return ProductListResponse.newBuilder()
            .addAllProducts(productList.map { it.toGrpcResponse() })
            .build()
    }
    
    override suspend fun updateStock(request: UpdateStockRequest): ProductResponse {
        val product = updateProductStockUseCase.updateStock(
            productId = request.productId,
            quantityChange = request.quantityChange
        )
        
        return product.toGrpcResponse()
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
