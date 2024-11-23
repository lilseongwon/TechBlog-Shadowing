package com.example.kotlinaop.common.cache

import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component

@Component
class CacheUser(
    _advice: CacheUserAdvice,
) {
    init {
        advice = _advice
    }

    companion object {
        private lateinit var advice: CacheUserAdvice
        private const val TOKEN = "::"

        fun <T> cache(vararg keys: Any, function: () -> T): T {
            return advice.cache(generateKey(keys), function)
        }

        fun <T> evict(vararg keys: Any, function: () -> T): T {
            return advice.evict(generateKey(keys), function)
        }

        private fun generateKey(keys: Array<out Any>) = keys.joinToString(TOKEN)
    }
}

@Component
class CacheUserAdvice {
    companion object {
        private const val CACHE_NAME = "User"
    }

    @Cacheable(value = [CACHE_NAME], key = "#key")
    fun <T> cache(key: String, function: () -> T): T {
        return function.invoke()
    }

    @CacheEvict(value = [CACHE_NAME], key = "#key")
    fun <T> evict(key: String, function: () -> T): T {
        return function.invoke();
    }
}