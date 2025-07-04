package com.vincenzo.shopping.payment

// TODO: 테스트 코드 업데이트 필요
/*
import com.vincenzo.shopping.payment.adapter.`in`.web.PaymentController
import com.vincenzo.shopping.payment.adapter.`in`.web.PaymentDetailRequest
import com.vincenzo.shopping.payment.adapter.`in`.web.ProcessPaymentRequest
import com.vincenzo.shopping.payment.application.service.PaymentService
import com.vincenzo.shopping.payment.domain.*
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class PaymentControllerTest {
    private lateinit var paymentController: PaymentController
    private lateinit var paymentService: PaymentService
    
    @BeforeEach
    fun setUp() {
        paymentService = mockk()
        paymentController = PaymentController(paymentService)
    }
    
    @Test
    fun `단일 결제 요청 테스트`() {
        // Given
        val request = ProcessPaymentRequest(
            orderId = 1L,
            memberId = 1L,
            totalAmount = 10000L,
            paymentDetails = listOf(
                PaymentDetailRequest(
                    method = PaymentMethod.PG_KPN,
                    amount = 10000L
                )
            )
        )
        
        val payment = Payment(
            id = 1L,
            orderId = 1L,
            memberId = 1L,
            totalAmount = 10000L,
            paymentMethod = PaymentMethod.PG_KPN,
            status = PaymentStatus.COMPLETED
        )
        
        every { paymentService.processPayment(any()) } returns payment
        
        // When
        val response = paymentController.processPayment(request)
        
        // Then
        assertEquals(HttpStatus.CREATED, response.statusCodeValue)
        assertEquals(1L, response.body?.paymentId)
    }
}
*/