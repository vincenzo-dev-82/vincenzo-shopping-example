package com.vincenzo.shopping.point

import com.vincenzo.shopping.point.application.port.`in`.ChargePointCommand
import com.vincenzo.shopping.point.application.port.`in`.UsePointCommand
import com.vincenzo.shopping.point.application.port.out.PointBalanceRepository
import com.vincenzo.shopping.point.application.port.out.PointTransactionRepository
import com.vincenzo.shopping.point.application.service.PointService
import com.vincenzo.shopping.point.domain.PointBalance
import com.vincenzo.shopping.point.domain.PointTransaction
import com.vincenzo.shopping.point.domain.TransactionType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

class PointServiceTest {

    private val pointBalanceRepository: PointBalanceRepository = mock(PointBalanceRepository::class.java)
    private val pointTransactionRepository: PointTransactionRepository = mock(PointTransactionRepository::class.java)
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
        
        whenever(pointBalanceRepository.findByMemberId(command.memberId)).thenReturn(balance)
        whenever(pointTransactionRepository.save(any())).thenReturn(transaction)
        whenever(pointBalanceRepository.save(any())).thenReturn(balance.copy(balance = balance.balance + command.amount))

        // when
        val result = pointService.chargePoint(command)

        // then
        assertEquals(TransactionType.CHARGE, result.type)
        assertEquals(command.amount, result.amount)
        
        verify(pointBalanceRepository, times(1)).findByMemberId(command.memberId)
        verify(pointTransactionRepository, times(1)).save(any())
        verify(pointBalanceRepository, times(1)).save(any())
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
        
        whenever(pointBalanceRepository.findByMemberId(command.memberId)).thenReturn(balance)
        whenever(pointTransactionRepository.save(any())).thenReturn(transaction)
        whenever(pointBalanceRepository.save(any())).thenReturn(balance.copy(balance = balance.balance - command.amount))

        // when
        val result = pointService.usePoint(command)

        // then
        assertEquals(TransactionType.USE, result.type)
        assertEquals(-command.amount, result.amount)
        
        verify(pointBalanceRepository, times(1)).findByMemberId(command.memberId)
        verify(pointTransactionRepository, times(1)).save(any())
        verify(pointBalanceRepository, times(1)).save(any())
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
        
        whenever(pointBalanceRepository.findByMemberId(command.memberId)).thenReturn(balance)

        // when & then
        assertThrows<IllegalArgumentException> {
            pointService.usePoint(command)
        }
        
        verify(pointBalanceRepository, times(1)).findByMemberId(command.memberId)
        verify(pointTransactionRepository, never()).save(any())
        verify(pointBalanceRepository, never()).save(any())
    }
}
