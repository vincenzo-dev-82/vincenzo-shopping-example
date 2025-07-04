package com.vincenzo.shopping.payment.adapter.out.processor

import com.vincenzo.shopping.payment.application.port.out.PaymentProcessor
import com.vincenzo.shopping.payment.application.port.out.PaymentResult
import com.vincenzo.shopping.payment.application.port.out.ValidationResult
import com.vincenzo.shopping.payment.domain.PaymentDetail
import com.vincenzo.shopping.payment.domain.PaymentMethod
import com.vincenzo.shopping.payment.domain.PaymentRules
import org.springframework.stereotype.Component

@Component
class CompositePaymentProcessor(
    private val paymentProcessors: List<PaymentProcessor>
) : PaymentProcessor {
    
    private val processorMap: Map<PaymentMethod, PaymentProcessor> = 
        paymentProcessors.associateBy { it.getSupportedMethod() }
    
    override fun getSupportedMethod(): PaymentMethod = PaymentMethod.COMPOSITE
    
    override suspend fun process(
        orderId: Long,
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any>
    ): PaymentResult {
        // 복합결제는 PaymentService에서 직접 처리하므로
        // 이 메서드는 사용되지 않음
        throw UnsupportedOperationException("복합결제는 PaymentService에서 직접 처리합니다.")
    }
    
    override suspend fun cancel(
        paymentDetail: PaymentDetail,
        reason: String
    ): PaymentResult {
        // 복합결제 취소도 PaymentService에서 개별 처리
        throw UnsupportedOperationException("복합결제 취소는 PaymentService에서 직접 처리합니다.")
    }
    
    override suspend fun validate(
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any>
    ): ValidationResult {
        println("복합결제 검증")
        
        // 복합결제는 항상 PG가 메인이어야 함
        val validationResult = PaymentRules.validateCompositePayment(
            mainMethod = PaymentMethod.PG_KPN,
            subMethods = listOf(PaymentMethod.CASHNOTE_POINT, PaymentMethod.COUPON)
        )
        
        return ValidationResult(
            isValid = validationResult.isValid,
            message = validationResult.message
        )
    }
}
