package com.vincenzo.shopping.payment.config

import com.vincenzo.shopping.payment.application.port.out.PaymentProcessor
import com.vincenzo.shopping.payment.application.processor.PaymentProcessorFactory
import com.vincenzo.shopping.payment.domain.PaymentValidationChain
import com.vincenzo.shopping.payment.domain.AmountValidationStrategy
import com.vincenzo.shopping.payment.domain.StandalonePaymentValidationStrategy
import com.vincenzo.shopping.payment.domain.CompositePaymentValidationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Payment 관련 설정 클래스
 * 
 * SOLID 원칙:
 * - Dependency Inversion Principle: 구체적인 구현이 아닌 추상화에 의존
 * - Single Responsibility Principle: 설정 관련 책임만 가짐
 */
@Configuration
class PaymentConfiguration {
    
    /**
     * 결제 검증 체인 설정
     * 여러 검증 전략을 연결하여 순차적으로 검증
     */
    @Bean
    fun paymentValidationChain(): PaymentValidationChain {
        val strategies = listOf(
            AmountValidationStrategy(),
            StandalonePaymentValidationStrategy(),
            CompositePaymentValidationStrategy()
        )
        
        return PaymentValidationChain(strategies)
    }
    
    /**
     * PaymentProcessorFactory는 @Component로 자동 등록되므로
     * 추가 설정이 필요한 경우 여기에 정의
     */
}
