package com.example.msatransaction.common

import java.net.SocketTimeoutException

sealed class ActResult<A> {
    abstract val resultType: ResultType

    internal class Success<A>(val data: A) : ActResult<A>() {
        override val resultType = ResultType.SUCCESS
    }

    internal class Failure<A>(
        val errorResponse: ErrorResponse
    ) : ActResult<A>() {
        override val resultType = ResultType.FAILURE
    }

    internal class Unknown<A>(
        val errorResponse: ErrorResponse
    ) : ActResult<A>() {
        override val resultType = ResultType.UNKNOWN
    }

    fun <C> map(
        f: (A) -> C
    ): ActResult<C> = when (this) {
        is Success -> Success(f(data))
        is Failure -> failure(errorResponse)
        is Unknown -> unknown(errorResponse)
    }

    fun <C> flatMap(
        f: (A) -> ActResult<C>
    ): ActResult<C> = when (this) {
        is Success -> f(data)
        is Failure -> failure(errorResponse)
        is Unknown -> unknown(errorResponse)
    }

    fun recoverUnknown(
        f: () -> ActResult<A>
    ): ActResult<A> = when (this) {
        is Success -> success(data)
        is Failure -> failure(errorResponse)
        is Unknown -> f()
    }

    fun onSuccess(f: (A) -> Unit): ActResult<A> = when (this) {
        is Success -> {
            f(data)
            this
        }

        is Failure -> failure(errorResponse)
        is Unknown -> unknown(errorResponse)
    }

    fun onFailure(f: (ErrorResponse) -> Unit): ActResult<A> = when (this) {
        is Success -> success(data)
        is Failure -> {
            f(errorResponse)
            this
        }

        is Unknown -> unknown(errorResponse)
    }

    fun onUnknown(f: (ErrorResponse) -> Unit): ActResult<A> = when (this) {
        is Success -> success(data)
        is Failure -> failure(errorResponse)
        is Unknown -> {
            f(errorResponse)
            this
        }
    }


    companion object {
        fun <A> success(data: A): ActResult<A> = Success(data)
        fun <A> failure(errorResponse: ErrorResponse): ActResult<A> = Failure(errorResponse)
        fun <A> unknown(errorResponse: ErrorResponse): ActResult<A> = Unknown(errorResponse)

        operator fun <A> invoke(func: () -> A): ActResult<A> =
            executeExceptionSafeContext(
                run = { Success(func()) },
                failure = { Failure(it) },
                unknown = { Unknown(it) }
            )
    }
}

inline fun <T> executeExceptionSafeContext(
    run: () -> T,
    failure: (e: ErrorResponse) -> T,
    unknown: (e: ErrorResponse) -> T,
) = try {
    run()
} catch (e: Exception) {
    with(e.toErrorResponse()) {
        when (this.getFailType()) {
            FailType.U -> unknown(this)
            FailType.F -> failure(this)
        }
    }
}


enum class ResultType {
    SUCCESS, FAILURE, UNKNOWN
}

fun Exception.toErrorResponse() = ErrorResponse(this)

data class ErrorResponse(val ex: Exception) {
    fun getFailType(): FailType = when (ex) {
        is RuntimeException -> FailType.F
        is SocketTimeoutException -> FailType.U
        else -> {
            println("unknown exception. $ex")
            FailType.U
        }
    }
}

enum class FailType {
    U, //Unknown
    F //Failure
}