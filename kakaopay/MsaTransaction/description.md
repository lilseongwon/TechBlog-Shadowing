## MSA 환경에서 네트워크 예외를 잘 다루는 방법

### 간단 요약
- 글로벌 트랜잭션 관리는 어떤 방식이든 네트워크 요청을 주고받음
- 즉 트랙잭션을 잘 다루려면, 네트워크 예외처리를 잘 해야함
- 이를 안전하게 다루기 위해 고려한 측면은 다음과 같음
  - API를 요청하는 입장 -> TimeOut 등의 결제 성공/실패 여부를 알 수 없는 예외를 처리하기 (retry)
  - API를 제공하는 입장 -> 멱등성 API 제공


### 구현 내용
1. 멱등성 API 보장을 위해 tx_key로 같은 결제요청인지 여부를 식별한다 ('따닥' 이슈는 없다고 가정한다)
   - 따닥 이슈는 일정 시간(ex-10초)의 ttl이 설정된 tx-key를 redis에 저장 후 요청 시 조회하여 해결 가능
   - tx-key가 없을 시 어플리케이션에서 생성
2. 코틀린 커스텀 클래스를 사용하여 반복되는 try-catch 문을 개선한다.

**reference:** https://tech.kakaopay.com/post/msa-transaction/

# 개념 정리
## Invoke

- 이름을 부여한 함수임에도 실행을 간편하게 할 수 있게 하는 것들을 연산자라고 한다.

ex) plus연산자 

```kotlin
object Sample {
    operator fun plus(str: String):String {
        return this.toString() + str
    }
}

main() {
	Sample + " Hello~!" //(Sample의 주소값) Hello
}
```

마찬가지로 invoke도 클래스를 함수처럼 호출할 수 있게 만들어주는 역할을 수행한다.

```kotlin
object MyFunction {
    operator fun invoke(str: String): String {
        return str.toUpperCase() // 모두 대문자로 바꿔줌
    }
}

MyFunction.invoke("hello") // HELLO
MyFunction("hello") // HELLO
```

## 고차 함수

- 다른 함수를 인자로 받거나 함수를 반환하는 함수
- 함수 타입을 정의하려면
    1. 함수 파라미터의 타입을 괄호 안에 넣고
    2. 그 뒤에 화살표(→)를 추가하고
    3. 함수의 반환 타입을 지정하면 된다
    
    `(Int, String) → Unit`
    

```kotlin
fun twoAndThree(operation: (Int, Int) -> Int) {
    val result = operation(2, 3)
    println("The result is $result")
}

fun main(args: Array<String>) {
    twoAndThree { a, b -> a + b }
    twoAndThree { a, b -> a * b }
}
```

## 실제 코드에서 어떻게 사용되는가?

```kotlin
sealed class ActResult<A> {
	...
  operator fun <A> invoke(func: () -> A): ActResult<A> =
     executeExceptionSafeContext(
          run = { Success(func()) }, //고차함수 사용
          /*
            여기서 it은 람다식 정의이므로 값이 없음. 
            함수 호출할 때 ErrorResponse가 전달됨.
            개인적으로 이 부분이 가장 신기했음
          */
          failure = { Failure(it) }, 
          unknown = { Unknown(it) }
    )
}

@Service
class PaymentService(
    private val paymentAdapter: PaymentAdapter
) {
    fun doPay(request: PayRequest): ActResult<String> =
        ActResult { this.validate(request) } //invoke
        ...
}
```

## 공변성

- `val anyCup: Cup<Any> = Cup<Int>()` 코드는 분명 Int는 Any의 서브타입이지만, 제네릭에서는 관계를 가지고 있지 않으므로 에러가 난다.

```kotlin
fun main() {
    val strs: ArrayList<String> = ArrayList()
    val objs: ArrayList<Any> = strs // 컴파일 에러가 발생하지 않는다면?

    objs.add(1)

    val s: String = strs[0] // ClassCastException: Cannot cast Integer to String
}
```

- 위 예제같이 제네릭 타입의 인스턴스에 예상치 못한 타입의 객체가 주입되는 것을 방지하여 타입 안정성을 향상시키기 위해 제네릭은 기본적으로 불변으로 설계되어 있다.
- 공변성(out)은 A가 B의 서브 타입일 때, Class<A>가 Class<B>의 서브 타입이라는 의미이다.
- 즉 아래와 같이 업캐스팅이 가능하다.

```kotlin
fun main() {
    val puppyBox: Box<Puppy> = Box()
    val dogBox: Box<Dog> = puppyBox // 업캐스팅
    
    val puppy = Puppy()
    val dog = puppy // 업캐스팅
}
```

- 하지만 아래와 같이 런타임 익셉션이 나는것을 방지하기 위해서 out 키워드와 함께 파라미터를 선언한 클래스에서는 메서드의 파라미터로 타입 파라미터를 선언하지 못하도록 강제한다

## 실제 코드에서 문제를 해결해보자

```kotlin
//기존 코드
sealed class ActResult<out A> {
	...
	fun recoverUnknown(
	//out 키워드 사용시 메서드 파라미터 타입으로 파라미터 타입 사용 불가
		f: () -> ActResult<A>
	): ActResult<A> = when(this) {
		isSuccess -> success(data)
		isFailure -> failure(errorResponse)
		isUnknown -> f()
	}
}

//수정된 코드 -> out 키워드 삭제
sealed class ActResult<T> {
	...
}
```
**invoke**: https://wooooooak.github.io/kotlin/2019/03/21/kotlin_invoke/

**inline function**: https://velog.io/@haero_kim/Kotlin-Inline-Function-%ED%8C%8C%ED%97%A4%EC%B9%98%EA%B8%B0

**고차 함수**: https://incheol-jung.gitbook.io/docs/study/kotlin-in-action/8#undefined-7

**공변성/반공변성(in/out)**: https://velog.io/@hoyaho/generic-2
