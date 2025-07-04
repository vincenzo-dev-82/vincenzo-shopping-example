package com.vincenzo.shopping.member.adapter.`in`.web

import com.vincenzo.shopping.member.application.port.`in`.CreateMemberCommand
import com.vincenzo.shopping.member.application.port.`in`.CreateMemberUseCase
import com.vincenzo.shopping.member.domain.Member
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/members")
class MemberController(
    private val createMemberUseCase: CreateMemberUseCase
) {
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createMember(@RequestBody request: CreateMemberRequest): MemberResponse {
        val command = CreateMemberCommand(
            email = request.email,
            name = request.name,
            phoneNumber = request.phoneNumber
        )
        val member = createMemberUseCase.createMember(command)
        return MemberResponse.from(member)
    }
    
    @GetMapping("/hello")
    fun hello(): String {
        return "Hello from Member Service!"
    }
}

data class CreateMemberRequest(
    val email: String,
    val name: String,
    val phoneNumber: String
)

data class MemberResponse(
    val id: Long?,
    val email: String,
    val name: String,
    val phoneNumber: String,
    val point: Int
) {
    companion object {
        fun from(member: Member): MemberResponse {
            return MemberResponse(
                id = member.id,
                email = member.email,
                name = member.name,
                phoneNumber = member.phoneNumber,
                point = member.point
            )
        }
    }
}
