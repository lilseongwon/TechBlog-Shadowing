package com.example.kotlinaop.user

import com.example.kotlinaop.common.and
import com.example.kotlinaop.common.cache.CacheUser
import com.example.kotlinaop.payment.PaymentRepository
import com.example.kotlinaop.roles.RoleRepository
import com.example.kotlinaop.common.transaction.Tx
import com.example.kotlinaop.user.dto.SignupRequest
import com.example.kotlinaop.user.dto.UserRead
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val paymentRepository: PaymentRepository,
    private val roleRepository: RoleRepository
) {
    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun signUp(request: SignupRequest) {
        logger.info("signUp() 시작")

        logger.info("saveUserData() 시작")
        this.saveUserData(User(
            name = request.name
        ))
        logger.info("saveUserData() 종료")

        logger.info("signUp() 종료")
    }

    private fun saveUserData(user: User) = Tx.writable {
        userRepository.save(user)
    }

    fun findById(userId: Long): UserRead = CacheUser.cache("UserRead", "userId:${userId}") {
        val user = userRepository.findById(userId).orElseThrow { throw Exception("User Not Found :${userId}") }
        return@cache UserRead(
            id = user.id,
            name = user.name
        )
    }

    fun example() {
        val (users, payments, roles) = Tx.readable {
            val users = userRepository.findAll()
            val payments = paymentRepository.findAll()
            val roles = roleRepository.findAll()

            return@readable users and payments and roles
        }
    }
}