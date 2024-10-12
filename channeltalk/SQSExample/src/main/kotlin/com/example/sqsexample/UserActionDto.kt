package com.example.sqsexample

data class UserActionDto(
    val userId: Long,
    val actionId: Long?,
    val actionType: ActionType
)