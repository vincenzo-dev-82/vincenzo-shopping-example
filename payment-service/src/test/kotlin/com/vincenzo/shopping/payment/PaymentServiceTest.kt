package com.vincenzo.shopping.payment

import com.vincenzo.shopping.payment.application.port.`in`.PaymentDetailCommand
import com.vincenzo.shopping.payment.application.port.`in`.ProcessPaymentCommand
import com.vincenzo.shopping.payment.application.port.out.PaymentRepository
import com.vincenzo.shopping.payment.application.service.PaymentService
import com.vincenzo.shopping.payment.domain.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify as verifyKotlin

class PaymentServiceTest {

    private val paymentRepository: PaymentRepository = mock(PaymentRepository::class.java)
    private val paymentProcessor: PaymentProcessor = mock(PaymentProcessor::class.java)
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

        whenever(paymentRepository.save(any())).thenReturn(payment)
        whenever(paymentProcessor.supports(PaymentMethod.PG_KPN)).thenReturn(true)
        whenever(paymentProcessor.process(any())).thenReturn(paymentResult)

        // when
        val result = paymentService.processPayment(command)

        // then
        assertNotNull(result)
        assertEquals(command.orderId, result.orderId)
        assertEquals(command.totalAmount, result.totalAmount)
        
        verifyKotlin(paymentRepository, atLeast(1)).save(any())
        verifyKotlin(paymentProcessor).process(any())
    }

    @Test
    fun `결제 처리 실패 - 쿠폰 단독 결제`() {
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
        
        verifyKotlin(paymentRepository, never()).save(any())
    }
}
