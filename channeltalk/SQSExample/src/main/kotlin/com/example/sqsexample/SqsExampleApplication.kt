package com.example.sqsexample

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SqsExampleApplication

fun main(args: Array<String>) {
    runApplication<SqsExampleApplication>(*args)
}
