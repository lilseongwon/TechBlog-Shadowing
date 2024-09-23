package com.example.msatransaction

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@EnableFeignClients
@SpringBootApplication
class MsaTransactionApplication

fun main(args: Array<String>) {
    runApplication<MsaTransactionApplication>(*args)
}
