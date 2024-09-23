package com.example.msatransaction.common

import com.example.msatransaction.order.PayRequest
import org.springframework.stereotype.Service

@Service
class PaymentAdapter(
    private val paymentClient: PaymentClient
) {
    fun pay(request: PayRequest): ActResult<String> {
        return ActResult { paymentClient.pay(request) }
    }

    fun validate(request: PayRequest): ActResult<String> {
        return ActResult { paymentClient.pay(request) }
    }
}