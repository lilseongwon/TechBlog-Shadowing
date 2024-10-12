package com.example.sqsexample

import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.operations.SendResult
import io.awspring.cloud.sqs.operations.SqsTemplate
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AmazonSqsSender(
    private val sqsTemplate: SqsTemplate,
    private val objectMapper: ObjectMapper
) {
    fun sendMessage(dto: UserActionDto): SendResult<String> {
        val request = objectMapper.writeValueAsString(dto)
        return sqsTemplate.send { sendOpsTo ->
            sendOpsTo
                .queue("SQS 대기열 이름")
                .messageGroupId("action-userId" + dto.userId)
                .payload(request)
                .messageDeduplicationId(UUID.randomUUID().toString())
        }
    }
}