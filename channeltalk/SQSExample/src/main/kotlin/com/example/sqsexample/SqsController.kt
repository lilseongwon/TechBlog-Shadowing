package com.example.sqsexample

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SqsController(
    private val sqsSender: AmazonSqsSender
) {
    @PostMapping("/send")
    fun send(@RequestBody request: UserActionDto) {
        sqsSender.sendMessage(request)
    }
}
