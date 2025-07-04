package com.vincenzo.shopping.payment.application.processor

import com.vincenzo.shopping.payment.application.port.out.PaymentProcessor
import com.vincenzo.shopping.payment.application.port.out.PaymentResult
import com.vincenzo.shopping.payment.application.port.out.ValidationResult
import com.vincenzo.shopping.payment.domain.PaymentDetail
import com.vincenzo.shopping.payment.domain.PaymentMethod
import kotlin.system.measureTimeMillis

/**
 * Decorator Pattern을 적용한 로깅 데코레이터
 * 
 * SOLID 원칙:
 * - Single Responsibility Principle: 로깅이라는 단일 책임만 가짐
 * - Open/Closed Principle: 기존 PaymentProcessor를 수정하지 않고 기능 추가
 * - Liskov Substitution Principle: PaymentProcessor 인터페이스를 완벽히 구현
 */
class LoggingPaymentProcessor(
    private val processor: PaymentProcessor
) : PaymentProcessor {
    
    override fun getSupportedMethod(): PaymentMethod = processor.getSupportedMethod()
    
    override suspend fun process(
        orderId: Long,
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any>
    ): PaymentResult {
        println("[Payment Log] 결제 시작 - 방법: ${getSupportedMethod()}, 주문: $orderId, 금액: $amount")
        
        val result: PaymentResult
        val elapsed = measureTimeMillis {
            result = processor.process(orderId, memberId, amount, metadata)
        }
        
        println("[Payment Log] 결제 완료 - 성공: ${result.success}, 소요시간: ${elapsed}ms, 메시지: ${result.message}")
        
        return result
    }
    
    override suspend fun cancel(
        paymentDetail: PaymentDetail,
        reason: String
    ): PaymentResult {
        println("[Payment Log] 결제 취소 시작 - 거래ID: ${paymentDetail.transactionId}, 사유: $reason")
        
        val result: PaymentResult
        val elapsed = measureTimeMillis {
            result = processor.cancel(paymentDetail, reason)
        }
        
        println("[Payment Log] 결제 취소 완료 - 성공: ${result.success}, 소요시간: ${elapsed}ms")
        
        return result
    }
    
    override suspend fun validate(
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any>
    ): ValidationResult {
        println("[Payment Log] 결제 검증 - 회원: $memberId, 금액: $amount")
        
        val result = processor.validate(memberId, amount, metadata)
        
        println("[Payment Log] 검증 결과 - 유효: ${result.isValid}, 메시지: ${result.message}")
        
        return result
    }
}

/**
 * Decorator Pattern을 적용한 모니터링 데코레이터
 * 
 * 실제 환경에서는 Micrometer 등을 사용하여 메트릭을 수집
 */
class MonitoringPaymentProcessor(
    private val processor: PaymentProcessor
) : PaymentProcessor {
    
    private var totalRequests = 0L
    private var successfulRequests = 0L
    private var failedRequests = 0L
    
    override fun getSupportedMethod(): PaymentMethod = processor.getSupportedMethod()
    
    override suspend fun process(
        orderId: Long,
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any>
    ): PaymentResult {
        totalRequests++
        
        val result = processor.process(orderId, memberId, amount, metadata)
        
        if (result.success) {
            successfulRequests++
        } else {
            failedRequests++
        }
        
        println("[Monitoring] ${getSupportedMethod()} - Total: $totalRequests, Success: $successfulRequests, Failed: $failedRequests")
        
        return result
    }
    
    override suspend fun cancel(
        paymentDetail: PaymentDetail,
        reason: String
    ): PaymentResult {
        return processor.cancel(paymentDetail, reason)
    }
    
    override suspend fun validate(
        memberId: Long,
        amount: Long,
        metadata: Map<String, Any>
    ): ValidationResult {
        return processor.validate(memberId, amount, metadata)
    }
}
