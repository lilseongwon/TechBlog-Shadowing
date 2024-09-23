package com.example.msatransaction.payment

import com.example.msatransaction.order.PayRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentService(
    private val paymentTransactionRepository: PaymentTransactionRepository
) {
    @Transactional
    fun pay(request: PayRequest) {
        //동일한 txKey 멱등성 보장
        paymentTransactionRepository.findByTxKey(request.txKey)?.let { return }

        //결제로직 생략

        paymentTransactionRepository.save(
            PaymentTransaction(
                txKey = request.txKey,
                amount = request.amount
            )
        )
    }
}