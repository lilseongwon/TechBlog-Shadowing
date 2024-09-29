## OpenAPI Specification을 이용한 더욱 효과적인 API 문서화

### 간단 요약&구현 내
**Swagger**

| 장점 | 단점 |
| --- | --- |
| 아름다운 문서 | 테스트가 없어 높지 않은 신뢰도 |
| API Test 기능 지원 | Swagger 어노테이션이 비즈니스 소스코드와 섞임 |

**Spring Rest Docs**

| **장점** | **단점** |
| --- | --- |
| 테스트 강제로 인한 높은 신뢰도 | 아름답지 않은 문서 |
| 비즈니스 소스코드에 영향 없음 | API Test 기능 미지원 |

Spring Rest Docs를 연동하여 OAS(OpenApiSpec. Swagger-ui로 시각화 가능) 파일을 생성하는 오픈소스를 사용해서 둘의 장점을 결합해보자

**reference**: https://tech.kakaopay.com/post/openapi-documentation/


# 개념정리
## Gradle이란?

- Groovy를 기반으로 한 빌드 툴

## 코드

```groovy
plugins {...} //Gradle Task의 집합
repositories {...} //각종 프로그램들이 저장되는 위치
dependencies {  //저장소에서 필요한 라이브러리를 사용할 수 있음
	compile(”library”) : 컴파일시 의존 라이브러리
	testCompile(”library”) : 테스트 컴파일시 의존 라이브러리
}

```

```groovy
tasks.register<Copy>("copyOasToSwagger") {
    delete("src/main/resources/static/swagger-ui/openapi3.yaml") // 기존 OAS 파일 삭제
    from("$buildDir/api-spec/openapi3.yaml") // 복제할 OAS 파일 지정
    into("src/main/resources/static/swagger-ui/.") // 타겟 디렉터리로 파일 복제
    dependsOn("openapi3") // openapi3 Task가 먼저 실행되도록 설정
}
```
`task.register<T>("name") { ... }`
- 원하는 작업을 추가할 수 있다.
- 제네릭으로 작업을 유형을 지정할 수 있다

### gradle 명령어

```bash
# 빌드
$ gradle build

# application 실행파일 실행
$ gradle run

# 빌드 파일 제거
$ gradle clean

# 기존 빌드 디렉토리 삭제 후 빌드
$ gradlew clean build
```

### Gradle Wrapper란?

Gradle은 다른 프로그램처럼 버전이 다르면 사용할 수 없다.

이를 해결하기 위해 매번 프로젝트를 받을 때마다 해당 프로젝트가 채택하는 Gradle의 버전을 다시 설치하는 것은 비효율적이다.

Gradle Wrapper는 내장 그레이들로써, 프로젝트별로 Gradle을 설치하지 않아도 해당 프로젝트의 Gradle 사용이 가능하다.

`./gradlew [작업명]` 명령어로 gradlew를 사용할 수 있다.

## Compile vs Implementation

![image](https://github.com/user-attachments/assets/b9d1457d-81b5-4f42-8da7-93b056f7283c)

둘 다 의존성 주입이라는 면은 같지만, compile은 A를 참조하는 모든 모듈이 rebuild되고, implementation은 A가 수정될때 직접적으로 참조하는 B만 rebuild된다. (성능 빠름)

https://m-falcon.tistory.com/735

https://bluayer.com/13

https://kotlinworld.com/314

