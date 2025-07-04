package com.vincenzo.shopping.payment.application.processor

import com.vincenzo.shopping.payment.application.port.out.PaymentProcessor
import com.vincenzo.shopping.payment.domain.PaymentMethod
import org.springframework.stereotype.Component

/**
 * Factory Pattern을 적용한 PaymentProcessor 생성 팩토리
 * 
 * SOLID 원칙:
 * - Open/Closed Principle: 새로운 결제 방법 추가 시 이 클래스 수정 없이 확장 가능
 * - Dependency Inversion Principle: 구체적인 구현체가 아닌 인터페이스에 의존
 */
@Component
class PaymentProcessorFactory(
    private val processors: List<PaymentProcessor>
) {
    private val processorMap: Map<PaymentMethod, PaymentProcessor> = 
        processors.associateBy { it.getSupportedMethod() }
    
    fun getProcessor(paymentMethod: PaymentMethod): PaymentProcessor {
        return processorMap[paymentMethod]
            ?: throw IllegalArgumentException("지원하지 않는 결제 방법입니다: $paymentMethod")
    }
    
    fun getProcessors(paymentMethods: List<PaymentMethod>): List<PaymentProcessor> {
        return paymentMethods.map { getProcessor(it) }
    }
    
    fun getAllProcessors(): List<PaymentProcessor> = processors
    
    fun isSupported(paymentMethod: PaymentMethod): Boolean {
        return processorMap.containsKey(paymentMethod)
    }
}
