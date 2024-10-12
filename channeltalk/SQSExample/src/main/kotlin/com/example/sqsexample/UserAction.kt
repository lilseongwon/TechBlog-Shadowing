package com.example.sqsexample

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class UserAction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val actionId: Long,

    val userId: Long,

    val actionType: ActionType
)

enum class ActionType {
    SCROLL,
    CLICK
}