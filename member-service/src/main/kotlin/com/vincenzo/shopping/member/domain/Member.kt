package com.vincenzo.shopping.member.domain

data class Member(
    val id: Long? = null,
    val email: String,
    val name: String,
    val phoneNumber: String
)