package com.example.msatransaction.payment

import com.example.msatransaction.order.PayRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PaymentController(
    private val paymentService: PaymentService
) {
    @PostMapping("/pay")
    fun doPay(
        @RequestBody request: PayRequest
    ) {
        paymentService.pay(request)
    }
}