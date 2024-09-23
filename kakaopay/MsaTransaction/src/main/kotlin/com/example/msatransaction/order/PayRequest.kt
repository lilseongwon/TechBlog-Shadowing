package com.example.msatransaction.order

data class PayRequest(
    val userId: Long,
    val txKey: String,
    val amount: Long
)