package com.vincenzo.shopping.point.config

import com.vincenzo.shopping.point.application.port.`in`.ChargePointCommand
import com.vincenzo.shopping.point.application.port.`in`.ChargePointUseCase
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataInitializer {
    
    @Bean
    fun initPointData(chargePointUseCase: ChargePointUseCase) = CommandLineRunner {
        // 테스트용 포인트 충전
        val memberIds = listOf(1L, 2L, 3L)
        
        memberIds.forEach { memberId ->
            try {
                val command = ChargePointCommand(
                    memberId = memberId,
                    amount = 100000,  // 10만 포인트
                    description = "초기 테스트 포인트 충전"
                )
                val transaction = chargePointUseCase.chargePoint(command)
                println("포인트 충전 완료: 회원 ID $memberId, 금액: ${transaction.amount}")
            } catch (e: Exception) {
                println("포인트 충전 실패: ${e.message}")
            }
        }
    }
}