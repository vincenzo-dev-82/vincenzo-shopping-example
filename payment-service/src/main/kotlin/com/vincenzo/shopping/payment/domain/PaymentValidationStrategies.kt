package com.vincenzo.shopping.payment.domain

/**
 * Strategy Pattern을 적용한 결제 검증 전략
 * 
 * SOLID 원칙:
 * - Single Responsibility Principle: 각 전략은 하나의 검증 규칙만 담당
 * - Interface Segregation Principle: 필요한 메서드만 포함된 간단한 인터페이스
 */
interface PaymentValidationStrategy {
    fun validate(request: PaymentValidationRequest): PaymentValidationResult
}

data class PaymentValidationRequest(
    val paymentMethod: PaymentMethod,
    val amount: Long,
    val memberId: Long,
    val metadata: Map<String, Any> = emptyMap()
)

data class PaymentValidationResult(
    val isValid: Boolean,
    val message: String,
    val details: Map<String, Any> = emptyMap()
)

/**
 * 단독 결제 가능 여부 검증 전략
 */
class StandalonePaymentValidationStrategy : PaymentValidationStrategy {
    override fun validate(request: PaymentValidationRequest): PaymentValidationResult {
        val canStandalone = when (request.paymentMethod) {
            PaymentMethod.PG_KPN -> true
            PaymentMethod.BNPL -> true
            PaymentMethod.CASHNOTE_POINT -> true
            PaymentMethod.POINT -> true  // CASHNOTE_POINT의 별칭
            PaymentMethod.COUPON -> false
            PaymentMethod.COMPOSITE -> true
        }
        
        return PaymentValidationResult(
            isValid = canStandalone,
            message = if (canStandalone) {
                "${request.paymentMethod} 단독 결제 가능"
            } else {
                "${request.paymentMethod}은(는) 단독 결제가 불가능합니다"
            }
        )
    }
}

/**
 * 복합 결제 조합 검증 전략
 */
class CompositePaymentValidationStrategy : PaymentValidationStrategy {
    override fun validate(request: PaymentValidationRequest): PaymentValidationResult {
        val mainMethod = request.metadata["mainMethod"] as? PaymentMethod
        val subMethods = request.metadata["subMethods"] as? List<PaymentMethod> ?: emptyList()
        
        // 복합결제는 반드시 PG가 메인이어야 함
        if (mainMethod != PaymentMethod.PG_KPN) {
            return PaymentValidationResult(
                isValid = false,
                message = "복합결제의 메인 결제수단은 PG(KPN)여야 합니다"
            )
        }
        
        // BNPL은 복합결제에 포함될 수 없음
        if (subMethods.contains(PaymentMethod.BNPL)) {
            return PaymentValidationResult(
                isValid = false,
                message = "BNPL은 복합결제에 포함될 수 없습니다"
            )
        }
        
        // 허용된 서브 결제수단: 포인트, 쿠폰
        val allowedSubMethods = setOf(PaymentMethod.CASHNOTE_POINT, PaymentMethod.COUPON)
        val invalidMethods = subMethods.filterNot { it in allowedSubMethods }
        
        if (invalidMethods.isNotEmpty()) {
            return PaymentValidationResult(
                isValid = false,
                message = "복합결제에 허용되지 않는 결제수단이 포함되어 있습니다: $invalidMethods"
            )
        }
        
        return PaymentValidationResult(
            isValid = true,
            message = "유효한 복합결제 조합입니다"
        )
    }
}

/**
 * 금액 검증 전략
 */
class AmountValidationStrategy : PaymentValidationStrategy {
    override fun validate(request: PaymentValidationRequest): PaymentValidationResult {
        if (request.amount <= 0) {
            return PaymentValidationResult(
                isValid = false,
                message = "결제 금액은 0보다 커야 합니다"
            )
        }
        
        // 결제 수단별 최소/최대 금액 제한
        val limits = when (request.paymentMethod) {
            PaymentMethod.PG_KPN -> 100L to 50_000_000L
            PaymentMethod.BNPL -> 10_000L to 5_000_000L
            PaymentMethod.CASHNOTE_POINT -> 1L to 10_000_000L
            PaymentMethod.POINT -> 1L to 10_000_000L  // CASHNOTE_POINT의 별칭
            PaymentMethod.COUPON -> 1L to 1_000_000L
            PaymentMethod.COMPOSITE -> 100L to 50_000_000L
        }
        
        if (request.amount < limits.first) {
            return PaymentValidationResult(
                isValid = false,
                message = "${request.paymentMethod} 최소 결제 금액은 ${limits.first}원입니다"
            )
        }
        
        if (request.amount > limits.second) {
            return PaymentValidationResult(
                isValid = false,
                message = "${request.paymentMethod} 최대 결제 금액은 ${limits.second}원입니다"
            )
        }
        
        return PaymentValidationResult(
            isValid = true,
            message = "유효한 결제 금액입니다"
        )
    }
}

/**
 * Chain of Responsibility Pattern을 적용한 검증 체인
 */
class PaymentValidationChain(
    private val strategies: List<PaymentValidationStrategy>
) {
    fun validate(request: PaymentValidationRequest): PaymentValidationResult {
        for (strategy in strategies) {
            val result = strategy.validate(request)
            if (!result.isValid) {
                return result
            }
        }
        
        return PaymentValidationResult(
            isValid = true,
            message = "모든 검증을 통과했습니다"
        )
    }
}