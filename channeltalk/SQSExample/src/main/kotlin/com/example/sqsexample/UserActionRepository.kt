package com.example.sqsexample

import org.springframework.data.repository.CrudRepository

interface UserActionRepository : CrudRepository<UserAction, Long> {
    fun existsByUserIdAndActionId(userId: Long, actionId: Long): Boolean
}