package com.vincenzo.shopping.payment.domain

import com.vincenzo.shopping.payment.application.processor.PaymentProcessor
import com.vincenzo.shopping.payment.application.processor.PaymentResult
import com.vincenzo.shopping.payment.application.processor.ValidationResult

/**
 * Domain에서 사용하는 PaymentProcessor 인터페이스
 * application.processor.PaymentProcessor를 상속받아 도메인 레이어에서도 사용
 */
interface PaymentProcessor : com.vincenzo.shopping.payment.application.processor.PaymentProcessor

// PaymentResult와 ValidationResult는 application.processor에서 import해서 사용
