package com.vincenzo.shopping.order.adapter.out.grpc

import com.vincenzo.shopping.grpc.product.GetProductRequest
import com.vincenzo.shopping.grpc.product.ProductServiceGrpcKt
import com.vincenzo.shopping.grpc.product.UpdateStockRequest
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component

@Component
class ProductServiceGrpcClient {
    
    @GrpcClient("product-service")
    private lateinit var productServiceStub: ProductServiceGrpcKt.ProductServiceCoroutineStub
    
    suspend fun getProduct(productId: Long): ProductInfo? {
        return try {
            val request = GetProductRequest.newBuilder()
                .setProductId(productId)
                .build()
            
            val response = productServiceStub.getProduct(request)
            
            ProductInfo(
                id = response.id,
                name = response.name,
                price = response.price,
                stock = response.stock,
                sellerId = response.sellerId
            )
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun updateStock(productId: Long, quantityChange: Int): ProductInfo? {
        return try {
            val request = UpdateStockRequest.newBuilder()
                .setProductId(productId)
                .setQuantityChange(quantityChange)
                .build()
            
            val response = productServiceStub.updateStock(request)
            
            ProductInfo(
                id = response.id,
                name = response.name,
                price = response.price,
                stock = response.stock,
                sellerId = response.sellerId
            )
        } catch (e: Exception) {
            null
        }
    }
}

data class ProductInfo(
    val id: Long,
    val name: String,
    val price: Long,
    val stock: Int,
    val sellerId: String
)
