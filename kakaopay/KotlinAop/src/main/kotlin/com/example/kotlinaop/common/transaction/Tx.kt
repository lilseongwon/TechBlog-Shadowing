package com.example.kotlinaop.common.transaction

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class Tx(
    _txAdvice: TxAdvice
) {
    init {
        txAdvice = _txAdvice
    }

    companion object {
        private lateinit var txAdvice: TxAdvice

        fun <T> writable(function: () -> T): T {
            return txAdvice.writable(function)
        }

        fun <T> readable(function: () -> T): T {
            return txAdvice.readable(function)
        }
    }
}

@Component
class TxAdvice {

    @Transactional
    fun <T> writable(function: () -> T): T {
        return function()
    }

    @Transactional(readOnly = true)
    fun <T> readable(function: () -> T): T {
        return function()
    }
}