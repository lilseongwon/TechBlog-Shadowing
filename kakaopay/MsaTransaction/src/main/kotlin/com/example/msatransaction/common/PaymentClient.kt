package com.example.msatransaction.common

import com.example.msatransaction.order.PayRequest
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(url = "http://localhost:8080")
interface PaymentClient {
    @PostMapping("/pay")
    fun pay(@RequestBody request: PayRequest): String

    @PostMapping("/validate")
    fun validate(@RequestBody request: PayRequest): String
}