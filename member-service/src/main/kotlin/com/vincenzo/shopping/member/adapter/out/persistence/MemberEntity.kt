package com.vincenzo.shopping.member.adapter.out.persistence

import jakarta.persistence.*

@Entity
@Table(name = "members")
class MemberEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(unique = true)
    val email: String,
    
    val name: String,
    
    val phoneNumber: String
)