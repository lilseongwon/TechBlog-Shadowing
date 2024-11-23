## Spring AOP의 아쉬운 점

- 익숙치않은 PointCut 문법을 사용해서 로직을 구현하는 것이 번거로울 수 있다
- 내부 함수 호출 적용이 불가하다
    - ex) IO 로직이 있는 메서드 전체에 트랜잭션을 걸면 IO 시간이 길어질수록 트랜잭션 비용이 증가하므로 DB insert/update하는 부분만 트랜잭션을 걸고 싶을 경우에서 사용 불가
    - 이걸 우회하기 위해 update 메서드를 다른 서비스 클래스에 구현해서 호출하는 방법이 있음 → AOP 한계를 희석하기 위해 장점을 희석하는 코드를 구현하게 됨
- 런타임 예외 발생 가능성
    - PointCut 표현식을 사용했을 때 실제로 적용되는지가 컴파일 단계에서는 검증이 불가.
    - JoinPoint의 인자값을 Array<Any>로 가져오게 되어 타입 문제로 런타임 에러 발생 가능
    - 어노테이션의 인자는 SpringELParser로 표현식을 쓰는데, 표현식의 인자명과 메서드의 인자명이 불일치할 경우에도 컴파일 예외가 발생하지 않아 추가로 검증 로직이 필요하다

## 구현내용

- Trailing Lambdas를 사용하여 Spring AOP Proxy객체 없이 @Transactional, @Cache 기능을 구현
- 인자를 받을 때 인자명 불일치로 인한 런타임 에러가 발생되는 한계를 극복

**reference:** https://tech.kakaopay.com/post/overcome-spring-aop-with-kotlin

# **개념 정리**

## Trailing Lambdas

- 마지막에 오는 함수 형태의 인자를 람다식으로 변환하여 넘겨주는 문법

```kotlin
val result = delegate({1+3})
```

Trailing Lambdas 문법 사용 후

```kotlin
val result = delegate {1 + 3}
```

## Companion Object 필드 빈 주입

```kotlin
@Component
class Tx(
    _txAdvice: TxAdvice
) {
    init {
        txAdvice = _txAdvice
    }

    companion object {
        private lateinit var txAdvice: TxAdvice

        fun <T> writable(function: () -> T): T {
            return txAdvice.writable(function)
        }

        fun <T> readable(function: () -> T): T {
            return txAdvice.readable(function)
        }
    }
}
```

txAdvice를 lateinit으로 설정하고 Tx 빈 등록시 주입된 _txAdvice 값으로 초기화함으로써 전역적으로 빈을 사용할 수 있게 된다.

## vararg

자바와 마찬가지로 코틀린도 vararg를 사용하면 호출할 때 인자 개수를 유동적으로 정할 수 있다.

```kotlin
fun sum(vararg num: Int) = num.sum()

fun main(args: Array<String>) {
    val n1 = sum(1)
    val n2 = sum(1, 2, 3, 4, 5)
    println(n1) // 1
    println(n2) // 15
}
```

## Lable 표현식

```kotlin
fun findById(userId: Long): UserRead = CacheUser.cache("UserRead", "userId:${userId}") {
		val user = userRepository.findById(userId).orElseThrow { throw Exception("User Not Found :${userId}") }
		return@cache UserRead( // @cache에 UserRead(...)를 return 한다
			id = user.id,
			name = user.name
		)
}
```

특정 루프나 코드 블록을 식별하기 위해 `@레이블이름` 으로 표현하는 문법.

break, continue, return 등의 흐름 제어에 사용된다.

## infix fun

```kotlin
infix fun <A, B> A.and(value: B) = Pair(this, value)
infix fun <A, B, C> Pair<A, B>.and(value: C) = Triple(this.first, this.second, value)
infix fun <A, B, C, D> Triple<A, B, C>.and(value: D) = Fourth(this.first, this.second, this.third, value)
infix fun <A, B, C, D, E> Fourth<A, B, C, D>.and(value: E) = Fifth(this.first, this.second, this.third, this.fourth, value)

----

fun example() {
    val (users, payments, roles) = Tx.readable {
    val users = userRepository.findAll()
    val payments = paymentRepository.findAll()
    val roles = roleRepository.findAll()

    **return@readable users and payments and roles**
	}
}
```

**두 개의 객체 중간에 들어가게 되는 함수 형태**

- `dispatcher.함수명(receiver): 리턴 타입 { ... }`으로 구현한다.
- `dispatcher 함수명 receiver` 로 사용한다