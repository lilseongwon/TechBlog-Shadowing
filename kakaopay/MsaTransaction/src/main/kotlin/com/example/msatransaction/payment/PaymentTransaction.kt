package com.example.msatransaction.payment

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table


@Entity
@Table(name = "payment_transaction")
class PaymentTransaction(
    @Id
    val txKey: String,
    val amount: Long
)