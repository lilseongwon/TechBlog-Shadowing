package com.example.kotlinaop.common

data class Fourth<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
)

data class Fifth<out A, out B, out C, out D, out E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

infix fun <A, B> A.and(value: B) = Pair(this, value)
infix fun <A, B, C> Pair<A, B>.and(value: C) = Triple(this.first, this.second, value)
infix fun <A, B, C, D> Triple<A, B, C>.and(value: D) = Fourth(this.first, this.second, this.third, value)
infix fun <A, B, C, D, E> Fourth<A, B, C, D>.and(value: E) = Fifth(this.first, this.second, this.third, this.fourth, value)