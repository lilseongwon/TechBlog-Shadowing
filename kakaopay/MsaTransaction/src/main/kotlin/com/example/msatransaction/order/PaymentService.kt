package com.example.msatransaction.order

import com.example.msatransaction.common.ActResult
import com.example.msatransaction.common.PaymentAdapter
import com.example.msatransaction.common.PaymentClient
import org.springframework.stereotype.Service

@Service
class PaymentService(
    private val paymentAdapter: PaymentAdapter
) {
    fun doPay(request: PayRequest): ActResult<String> =
        ActResult { this.validate(request) }
            .flatMap {
                paymentAdapter.pay(request)
                    .recoverUnknown { paymentAdapter.pay(request) }
            }
            .onSuccess { markSuccess() }
            .onFailure { markFailure() }
            .onUnknown { markUnknown() }
            .map { payResult -> "SUCCESS" }


    private fun validate(request: PayRequest) =
        ActResult { paymentAdapter.validate(request) }
            .onSuccess { println("validate success.") }
            .onFailure { println("validate failed.") }
            .onUnknown { println("validate unknown.") }

    private fun markSuccess() {
        print("payment success")
    }

    private fun markFailure() {
        print("payment failed")
    }

    private fun markUnknown() {
        print("payment unknown")
    }
}