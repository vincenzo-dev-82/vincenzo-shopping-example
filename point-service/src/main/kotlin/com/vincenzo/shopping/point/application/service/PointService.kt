package com.vincenzo.shopping.point.application.service

import com.vincenzo.shopping.point.application.port.`in`.*
import com.vincenzo.shopping.point.application.port.out.PointBalanceRepository
import com.vincenzo.shopping.point.application.port.out.PointTransactionRepository
import com.vincenzo.shopping.point.domain.PointBalance
import com.vincenzo.shopping.point.domain.PointTransaction
import com.vincenzo.shopping.point.domain.TransactionType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class PointService(
    private val pointBalanceRepository: PointBalanceRepository,
    private val pointTransactionRepository: PointTransactionRepository
) : UsePointUseCase, ChargePointUseCase, RefundPointUseCase, GetPointBalanceQuery {
    
    override fun usePoint(command: UsePointCommand): PointTransaction {
        // 잔액 확인
        val balance = pointBalanceRepository.findByMemberId(command.memberId)
            ?: throw IllegalStateException("포인트 잔액이 없습니다. 회원 ID: ${command.memberId}")
        
        if (balance.balance < command.amount) {
            throw IllegalArgumentException("포인트가 부족합니다. 현재 잔액: ${balance.balance}, 요청 금액: ${command.amount}")
        }
        
        // 트랜잭션 생성
        val transaction = PointTransaction(
            memberId = command.memberId,
            amount = -command.amount,  // 사용은 음수
            type = TransactionType.USE,
            description = command.description,
            referenceId = command.referenceId
        )
        pointTransactionRepository.save(transaction)
        
        // 잔액 업데이트
        val updatedBalance = balance.copy(
            balance = balance.balance - command.amount,
            updatedAt = LocalDateTime.now()
        )
        pointBalanceRepository.save(updatedBalance)
        
        return transaction
    }
    
    override fun chargePoint(command: ChargePointCommand): PointTransaction {
        // 잔액 조회 또는 생성
        val balance = pointBalanceRepository.findByMemberId(command.memberId)
            ?: PointBalance(
                memberId = command.memberId,
                balance = 0
            )
        
        // 트랜잭션 생성
        val transaction = PointTransaction(
            memberId = command.memberId,
            amount = command.amount,  // 충전은 양수
            type = TransactionType.CHARGE,
            description = command.description
        )
        pointTransactionRepository.save(transaction)
        
        // 잔액 업데이트
        val updatedBalance = balance.copy(
            balance = balance.balance + command.amount,
            updatedAt = LocalDateTime.now()
        )
        pointBalanceRepository.save(updatedBalance)
        
        return transaction
    }
    
    override fun refundPoint(command: RefundPointCommand): PointTransaction {
        // 잔액 조회 또는 생성
        val balance = pointBalanceRepository.findByMemberId(command.memberId)
            ?: PointBalance(
                memberId = command.memberId,
                balance = 0
            )
        
        // 트랜잭션 생성
        val transaction = PointTransaction(
            memberId = command.memberId,
            amount = command.amount,  // 환불은 양수
            type = TransactionType.REFUND,
            description = command.description,
            referenceId = command.referenceId
        )
        pointTransactionRepository.save(transaction)
        
        // 잔액 업데이트
        val updatedBalance = balance.copy(
            balance = balance.balance + command.amount,
            updatedAt = LocalDateTime.now()
        )
        pointBalanceRepository.save(updatedBalance)
        
        return transaction
    }
    
    @Transactional(readOnly = true)
    override fun getBalance(memberId: Long): PointBalance? {
        return pointBalanceRepository.findByMemberId(memberId)
    }
    
    @Transactional(readOnly = true)
    override fun getTransactionHistory(memberId: Long): List<PointTransaction> {
        return pointTransactionRepository.findByMemberId(memberId)
    }
}