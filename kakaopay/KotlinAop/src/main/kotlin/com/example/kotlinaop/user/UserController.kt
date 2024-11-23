package com.example.kotlinaop.user

import com.example.kotlinaop.user.dto.SignupRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/user")
@RestController
class UserController(
    private val userService: UserService
) {
    @PostMapping
    fun signUp(@RequestBody request: SignupRequest) {
        userService.signUp(request)
    }
}