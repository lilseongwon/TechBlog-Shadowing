## AWS SQS 도입 과정
reference: https://channel.io/ko/blog/tech-backend-aws-sqs-introduction

### 구현 내용
해당 아티클을 토대로 유저의 활동을 추적하는 API 설계

### 간단 요약
![image](https://github.com/user-attachments/assets/c4404276-2482-4518-8295-809d2c423999)

트래픽 증가시 기본적으로 오토 스케일링을 하지만, Spike 트래픽이 들어올때는 스케일링 하는동안 장애를 겪게 됨.

여기서 방법은 두가지

1. 요청을 일단 queue에 넣고 요청을 Buffer에서 빼면서 차례대로 처리 → 어차피 확인만 하면 되는 로깅
2. 허용된 용량 이상의 요청은 즉시 거절 → Open AP 등의 동기 처리 API에 사용

## AWS SQS

![image](https://github.com/user-attachments/assets/3396e1e4-3301-4481-93d6-9bd4ed27c579)

메세지를 queue에서 빼오는 것만으로는 SQS에서 메세지 삭제 안됨. 처리 후 ACK을 요청해야 함.

kafka, Aws Kinesis같은 messging queue는 “어디까지 처리했다”와 같은 checkpoint 방식으로 메세지 처리를 관리, SQS는 메세지 하나하나에 ACK/NACK을 보낼 수 있다는게 특징

## code example

```jsx
function worker() {

  while (true) {
    const message = sqs.receive()

    try {
      process(message) // 요청을 실제로 처리하는 부분 (비즈니스 로직)
      sqs.ack(message)
    } catch (e) {
      sqs.nack(message)
    }
  }

}
```

해당 구현에서 만약 process(), ack(), nack() 도중에 consumer process가 어떤 이유로 죽어버린 상황에서는 SQS가 ack/nack을 받지 못한 채 계속 대기하는 문제가 발생

따라서 Fallback을 위해 TimeOut을 추가 + Process도 Timeout 이상으로 시간이 걸리면 다른 worker에서도 처리하는 문제 방지를 위해 process()에도 timeout 걸기

```jsx
function worker() {

  while (true) {
    const message = sqs.receive({ timeout })

    try {
      withTimeout(process(message), timeout)
      sqs.ack(message)
    } catch (e) {
      if (e instanceof TimeoutException) {
        /* just pass, no need to do something. */
        continue
      }
      sqs.nack(message)
    }
  }

}
```

Consumer 프로세스가 죽는 경우에는 메세지 처리시 process를 끝내거나 도중에 죽는 경우가 존재. 비즈니스 로직과 SQS에 처리여부를 보내는게 atomic하지 않기 때문에 해당 메세지 중복 처리 가능성이 있음.

upsert → ㄱㅊ

pusher → 중복 제거 필터링

![image](https://github.com/user-attachments/assets/4ff49112-e2c2-426d-9138-7aaf94d5d82c)

### publisher deduplication

![image](https://github.com/user-attachments/assets/31378b8f-b002-4cb2-b654-effa2d097786)

publish되다가 실패시, SQS에 메세지가 도달하지 못했는지, 도달했는데 응답을 못받은건지 모르므로 재시도하게된다.

![image](https://github.com/user-attachments/assets/5446a454-8923-4525-a0ba-a288e317aa32)


따라서 SQS에서 `MessageDeduplicationId`를 지정해서 SQS가 중복 요청을 무시하도록 할 수 있음.

## Message Ordering

SQS에서는 kafka등과 달리 group(topic)을 미리 등록할 필요 없이 요청으로 무한히 사용/생성 가능하다. 또한 같은 group id를 가지는 메세지끼리는 처리 순서를 보장한다.

![image](https://github.com/user-attachments/assets/8148dcca-1dd0-4634-852d-eb97db9b0111)

SQS에서 같은 group id의 메세지는 FIFO이므로 한 message group에서는 가장 앞에 있는 메시지 하나만 읽을 수 있음. + 읽은 메세지가 ACK되어야 그 뒤에 있는 메시지를 가져올 수 있게 된다.

따라서 앞 메세지가 처리 실패시 뒤에 있는 메시지 처리가 불가하므로 SQS에서는 retry 최대 횟수를 두고, 이 이상 실패하면 dead-letter queue로 보내는 기능을 제공.

### SQS group 활용 예시

- 사용자 이벤트 추적 → 페이지 방문, 스크롤 내리기 등의 사용자 행동을 message group으로 순서대로 관리 가능.
- 채팅 메시지 스트림 → 채팅도 메시지의 순서가 유지된다. 채널톡 외부의 이메일, 문자 등의 외부 서비스 문의도 message group으로 관리하는 웹훅을 통해 채널톡 안에서 채팅으로 만들어진다.

### 메시지 처리 속도

기존에는 이미 인스턴스 안의 job queue에 들어온 요청은 해당 인스턴스가 다 처리해줘야 해서 인스턴스가 죽으면 job이 유실됨. → job이 많이 쌓인 상황에서 서버를 더 올려도 처리 속도가 별로 안늘어남.

SQS를 사용하여 Queue를 외부로 빼면 Consumer worker를 더 띄울수록 속도가 빨라진다.

![image](https://github.com/user-attachments/assets/d7095b45-3492-4894-9b8f-d64a1d7afde6)

## 운영 사례 - 서비스 요청 로깅

![image](https://github.com/user-attachments/assets/d6ee4c29-f25f-4526-81db-198330bc6125)

갑자기 많은 로그를 write하려면 DynamoDB의 provisioned throughput을 넘어서 문제가 발생. → 중간에 버퍼링을 위해 SQS를 넣고 일정한 속도로 메시지를 읽어나가며 storage로 보내는 디자인 선택
