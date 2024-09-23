## MSA 환경에서 네트워크 예외를 잘 다루는 방법

---
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