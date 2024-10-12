package com.example.sqsexample

import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.annotation.SqsListener
import io.awspring.cloud.sqs.listener.acknowledgement.Acknowledgement
import org.springframework.messaging.MessageHeaders
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AmazonSqsListener(
    private val userActionRepository: UserActionRepository,
    private val objectMapper: ObjectMapper
) {
    @SqsListener(value = ["SQS 대기열 이름"], messageVisibilitySeconds = "10")
    @Transactional(timeout = 10)
    fun listen(message: String, @Headers headers: MessageHeaders, acknowledgement: Acknowledgement) {
        val request = objectMapper.readValue(message, UserActionDto::class.java)
        //TODO acitonId없을시 유저의 첫 트래픽이라고 가정 후 action도 create
        userActionRepository.save(
            UserAction(
                actionId = request.actionId!!,
                userId = request.userId,
                actionType = request.actionType
            )
        )

        acknowledgement.acknowledge()
    }
}