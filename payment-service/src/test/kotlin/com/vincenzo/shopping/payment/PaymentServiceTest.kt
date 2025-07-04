package com.vincenzo.shopping.payment

import com.vincenzo.shopping.payment.application.port.`in`.PaymentDetailCommand
import com.vincenzo.shopping.payment.application.port.`in`.ProcessPaymentCommand
import com.vincenzo.shopping.payment.application.port.out.PaymentRepository
import com.vincenzo.shopping.payment.application.service.PaymentService
import com.vincenzo.shopping.payment.domain.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PaymentServiceTest {

    private val paymentRepository: PaymentRepository = mockk()
    private val paymentProcessor: PaymentProcessor = mockk()
    private val paymentService = PaymentService(paymentRepository, listOf(paymentProcessor))

    @Test
    fun `결제 처리 성공`() {
        // given
        val command = ProcessPaymentCommand(
            orderId = 1L,
            memberId = 1L,
            totalAmount = 10000,
            paymentDetails = listOf(
                PaymentDetailCommand(
                    method = PaymentMethod.PG_KPN,
                    amount = 10000,
                    metadata = emptyMap()
                )
            )
        )

        val payment = Payment(
            id = 1L,
            orderId = command.orderId,
            paymentDetails = listOf(
                PaymentDetail(
                    method = PaymentMethod.PG_KPN,
                    amount = 10000,
                    metadata = mapOf(
                        "member_id" to "1",
                        "order_id" to "1"
                    )
                )
            ),
            totalAmount = command.totalAmount,
            status = PaymentStatus.PROCESSING
        )

        val paymentResult = PaymentResult(
            success = true,
            transactionId = "TXN-001",
            message = "결제 성공"
        )

        every { paymentRepository.save(any()) } returns payment
        every { paymentProcessor.supports(PaymentMethod.PG_KPN) } returns true
        every { paymentProcessor.process(any()) } returns paymentResult

        // when
        val result = paymentService.processPayment(command)

        // then
        assertNotNull(result)
        assertEquals(command.orderId, result.orderId)
        assertEquals(command.totalAmount, result.totalAmount)
        
        verify(atLeast = 1) { paymentRepository.save(any()) }
        verify(exactly = 1) { paymentProcessor.process(any()) }
    }

    @Test
    fun `결제 처리 실패 - 쟁번 단독 결제`() {
        // given
        val command = ProcessPaymentCommand(
            orderId = 1L,
            memberId = 1L,
            totalAmount = 10000,
            paymentDetails = listOf(
                PaymentDetailCommand(
                    method = PaymentMethod.COUPON,
                    amount = 10000,
                    metadata = emptyMap()
                )
            )
        )

        // when & then
        assertThrows<IllegalArgumentException> {
            paymentService.processPayment(command)
        }
        
        verify(exactly = 0) { paymentRepository.save(any()) }
    }
}