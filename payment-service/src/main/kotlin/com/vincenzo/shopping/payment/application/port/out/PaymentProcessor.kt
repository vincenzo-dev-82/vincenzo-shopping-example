package com.vincenzo.shopping.payment.application.port.out

import com.vincenzo.shopping.payment.domain.PaymentDetail
import com.vincenzo.shopping.payment.domain.PaymentMethod

/**
 * 결제 처리기 인터페이스
 * 
 * SOLID 원칙:
 * - Interface Segregation Principle: 필요한 메서드만 포함
 * - Dependency Inversion Principle: 추상화에 의존
 */
interface PaymentProcessor {
    fun getSupportedMethod(): PaymentMethod
    
    suspend fun process(
        orderId: Long,
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any> = emptyMap()
    ): PaymentResult
    
    suspend fun cancel(
        paymentDetail: PaymentDetail,
        reason: String
    ): PaymentResult
    
    suspend fun validate(
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any> = emptyMap()
    ): ValidationResult
}

data class PaymentResult(
    val success: Boolean,
    val transactionId: String? = null,
    val message: String? = null,
    val processedAmount: Long? = null,
    val metadata: Map<String, Any> = emptyMap()
)

data class ValidationResult(
    val isValid: Boolean,
    val message: String? = null,
    val availableAmount: Long? = null
)