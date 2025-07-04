package com.vincenzo.shopping.payment.application.port.out

import com.vincenzo.shopping.payment.domain.PaymentDetail
import com.vincenzo.shopping.payment.domain.PaymentMethod

/**
 * Outbound Port: 결제 처리를 위한 인터페이스
 * 
 * 헥사고날 아키텍처에서 애플리케이션이 외부 결제 시스템과 통신하기 위한 포트
 * 각 결제 방법별로 Adapter에서 구현체를 제공
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
