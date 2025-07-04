package com.vincenzo.shopping.point

import com.vincenzo.shopping.point.application.port.`in`.ChargePointCommand
import com.vincenzo.shopping.point.application.port.`in`.UsePointCommand
import com.vincenzo.shopping.point.application.port.out.PointBalanceRepository
import com.vincenzo.shopping.point.application.port.out.PointTransactionRepository
import com.vincenzo.shopping.point.application.service.PointService
import com.vincenzo.shopping.point.domain.PointBalance
import com.vincenzo.shopping.point.domain.PointTransaction
import com.vincenzo.shopping.point.domain.TransactionType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PointServiceTest {

    private val pointBalanceRepository: PointBalanceRepository = mockk()
    private val pointTransactionRepository: PointTransactionRepository = mockk()
    private val pointService = PointService(pointBalanceRepository, pointTransactionRepository)

    @Test
    fun `포인트 충전 성공`() {
        // given
        val command = ChargePointCommand(
            memberId = 1L,
            amount = 10000,
            description = "테스트 충전"
        )
        
        val balance = PointBalance(
            id = 1L,
            memberId = command.memberId,
            balance = 5000
        )
        
        val transaction = PointTransaction(
            id = 1L,
            memberId = command.memberId,
            amount = command.amount,
            type = TransactionType.CHARGE,
            description = command.description
        )
        
        every { pointBalanceRepository.findByMemberId(command.memberId) } returns balance
        every { pointTransactionRepository.save(any()) } returns transaction
        every { pointBalanceRepository.save(any()) } returns balance.copy(balance = balance.balance + command.amount)

        // when
        val result = pointService.chargePoint(command)

        // then
        assertEquals(TransactionType.CHARGE, result.type)
        assertEquals(command.amount, result.amount)
        
        verify(exactly = 1) { pointBalanceRepository.findByMemberId(command.memberId) }
        verify(exactly = 1) { pointTransactionRepository.save(any()) }
        verify(exactly = 1) { pointBalanceRepository.save(any()) }
    }

    @Test
    fun `포인트 사용 성공`() {
        // given
        val command = UsePointCommand(
            memberId = 1L,
            amount = 3000,
            description = "테스트 사용",
            referenceId = "ORDER-001"
        )
        
        val balance = PointBalance(
            id = 1L,
            memberId = command.memberId,
            balance = 5000
        )
        
        val transaction = PointTransaction(
            id = 1L,
            memberId = command.memberId,
            amount = -command.amount,
            type = TransactionType.USE,
            description = command.description,
            referenceId = command.referenceId
        )
        
        every { pointBalanceRepository.findByMemberId(command.memberId) } returns balance
        every { pointTransactionRepository.save(any()) } returns transaction
        every { pointBalanceRepository.save(any()) } returns balance.copy(balance = balance.balance - command.amount)

        // when
        val result = pointService.usePoint(command)

        // then
        assertEquals(TransactionType.USE, result.type)
        assertEquals(-command.amount, result.amount)
        
        verify(exactly = 1) { pointBalanceRepository.findByMemberId(command.memberId) }
        verify(exactly = 1) { pointTransactionRepository.save(any()) }
        verify(exactly = 1) { pointBalanceRepository.save(any()) }
    }

    @Test
    fun `포인트 부족으로 사용 실패`() {
        // given
        val command = UsePointCommand(
            memberId = 1L,
            amount = 10000,
            description = "테스트 사용",
            referenceId = "ORDER-001"
        )
        
        val balance = PointBalance(
            id = 1L,
            memberId = command.memberId,
            balance = 5000  // 잔액보다 많이 사용하려고 함
        )
        
        every { pointBalanceRepository.findByMemberId(command.memberId) } returns balance

        // when & then
        assertThrows<IllegalArgumentException> {
            pointService.usePoint(command)
        }
        
        verify(exactly = 1) { pointBalanceRepository.findByMemberId(command.memberId) }
        verify(exactly = 0) { pointTransactionRepository.save(any()) }
        verify(exactly = 0) { pointBalanceRepository.save(any()) }
    }
}