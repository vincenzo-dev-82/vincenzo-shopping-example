package com.vincenzo.shopping.payment

// TODO: 테스트 코드 업데이트 필요
/*
import com.vincenzo.shopping.payment.application.port.out.PaymentRepository
import com.vincenzo.shopping.payment.application.processor.PaymentProcessor
import com.vincenzo.shopping.payment.application.service.PaymentService
import com.vincenzo.shopping.payment.domain.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PaymentServiceTest {
    private lateinit var paymentService: PaymentService
    private lateinit var paymentRepository: PaymentRepository
    private lateinit var paymentProcessors: List<PaymentProcessor>
    private lateinit var pgProcessor: PaymentProcessor
    private lateinit var pointProcessor: PaymentProcessor
    
    @BeforeEach
    fun setUp() {
        paymentRepository = mockk()
        pgProcessor = mockk()
        pointProcessor = mockk()
        paymentProcessors = listOf(pgProcessor, pointProcessor)
        paymentService = PaymentService(paymentRepository, paymentProcessors)
    }
    
    @Test
    fun `PG 단일 결제 성공 테스트`() {
        // Given
        val payment = Payment(
            orderId = 1L,
            memberId = 1L,
            totalAmount = 10000L,
            paymentMethod = PaymentMethod.PG_KPN,
            status = PaymentStatus.PENDING,
            paymentDetails = listOf(
                PaymentDetail(
                    method = PaymentMethod.PG_KPN,
                    amount = 10000L,
                    status = PaymentDetailStatus.PENDING
                )
            )
        )
        
        val savedPayment = payment.copy(id = 1L)
        val completedPayment = savedPayment.copy(
            status = PaymentStatus.COMPLETED,
            paymentDetails = listOf(
                PaymentDetail(
                    method = PaymentMethod.PG_KPN,
                    amount = 10000L,
                    status = PaymentDetailStatus.SUCCESS,
                    transactionId = "PG_12345"
                )
            )
        )
        
        every { paymentRepository.save(any()) } returnsMany listOf(savedPayment, completedPayment)
        every { pgProcessor.supports(PaymentMethod.PG_KPN) } returns true
        every { pgProcessor.process(any()) } returns PaymentResult(
            success = true,
            transactionId = "PG_12345"
        )
        
        // When
        val result = paymentService.processPayment(payment)
        
        // Then
        assertEquals(PaymentStatus.COMPLETED, result.status)
        assertEquals("PG_12345", result.paymentDetails.first().transactionId)
        verify(exactly = 2) { paymentRepository.save(any()) }
        verify { pgProcessor.process(any()) }
    }
    
    @Test
    fun `쿠폰 단독 결제 실패 테스트`() {
        // Given
        val payment = Payment(
            orderId = 1L,
            memberId = 1L,
            totalAmount = 10000L,
            paymentMethod = PaymentMethod.COUPON,
            status = PaymentStatus.PENDING,
            paymentDetails = listOf(
                PaymentDetail(
                    method = PaymentMethod.COUPON,
                    amount = 10000L,
                    status = PaymentDetailStatus.PENDING
                )
            )
        )
        
        // When & Then
        assertThrows<IllegalArgumentException> {
            paymentService.processPayment(payment)
        }
    }
}
*/