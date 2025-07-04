package com.vincenzo.shopping.payment.application.processor

import com.vincenzo.shopping.payment.domain.PaymentDetail
import com.vincenzo.shopping.payment.domain.PaymentMethod

/**
 * Strategy Pattern을 위한 PaymentProcessor 인터페이스
 * 각 결제 방법별로 구현체를 만들어 SOLID 원칙 준수
 */
interface PaymentProcessor {
    /**
     * 지원하는 결제 방법
     */
    fun getSupportedMethod(): PaymentMethod
    
    /**
     * 결제 처리
     */
    suspend fun process(
        orderId: Long,
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any> = emptyMap()
    ): PaymentResult
    
    /**
     * 결제 취소
     */
    suspend fun cancel(
        paymentDetail: PaymentDetail,
        reason: String
    ): PaymentResult
    
    /**
     * 결제 검증
     */
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
    val processedAmount: Long = 0,
    val metadata: Map<String, Any> = emptyMap()
)

data class ValidationResult(
    val isValid: Boolean,
    val message: String? = null,
    val availableAmount: Long? = null
)