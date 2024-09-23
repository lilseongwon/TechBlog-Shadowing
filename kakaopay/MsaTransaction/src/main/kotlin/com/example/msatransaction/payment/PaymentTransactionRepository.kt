package com.example.msatransaction.payment

import org.springframework.data.repository.CrudRepository

interface PaymentTransactionRepository : CrudRepository<PaymentTransaction, String> {
    fun findByTxKey(txKey: String): PaymentTransaction?
}