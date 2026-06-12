# GMG(가면가)

| <img width="251" height="520" alt="image" src="https://github.com/user-attachments/assets/dee88061-eac3-4951-af87-b50f977f68a8" /> | <img width="251" height="520" alt="image" src="https://github.com/user-attachments/assets/6ad969e7-7bc0-4a60-98ef-6e38032f51a6" /> |
|:--:|:--:|

## 프로젝트 소개
GMG는 모임 생성자가 가능한 날짜, 시간, 장소 조건을 공유하면 참여자들이 가능한 시간을 입력하고 선호하지 않는 장소를 제외해 최종 모임 후보를 좁히는 서비스입니다.

## Problem(문제)
- "우리 다음 주에 한번 보자!" 말은 쉽지만, 약속을 잡는 과정은 즐거움보다 피로감을 먼저 안겨줌"
- "사람들은 약속을 잡을 때 가능한 시간, 가고 싶은 장소를 생각하고 조율하는 것이 많은 시간을 소비함"

## 주요 기능
- 모임 생성 : 장소 유형, 날짜, 위치, 모임 정보를 입력해 공유 가능한 모임을 생성합니다.
- 참여자 관리 : 참여자 이름 등록, 중복 확인, 입력 완료 상태 전환을 처리합니다.
- 불가능 시간 등록 : 참여자가 선택 날짜 안에서 불가능한 시간대를 등록하고 조회할 수 있게 합니다.
- 히트맵 계산 : 완료된 참여자의 불가능 시간을 30분 단위 슬롯으로 집계해 가능 인원과 강도를 계산합니다.
- 실시간 갱신 : 참여자 입력 이후 히트맵과 장소 추천 결과를 SSE로 브로드캐스트합니다.
- 장소 검색/추천 : 카카오맵 API로 장소를 수집, Google Maps API로 상세 정보와 이미지를 보강하며, 참여자 비선호와 영업일/시간 접합도를 반영해 추천합니다
- 피드백 수집 : 사용자 피드백을 저장합니다

## 기술 스택 
- Java 21
- Spring Boot 3.5
- Spring Web MVC
- Spring Data JPA
- Flyway
- MySQL, H2

## 구조
```text
backend/
├── src/main/java/eusyaeusya/gmg
│   ├── api/          # HTTP controller, request/response DTO, API spec
│   ├── common/       # 공통 응답, 예외 처리, 감사 필드, 로깅
│   ├── config/       # security, async, SSE, swagger, local seed 설정
│   ├── domain/       # event, participant, place, feedback 도메인
│   └── infra/        # Kakao Map, Google Maps 외부 API 클라이언트
├── src/main/resources
│   ├── application*.yml
│   ├── db/migration/
│   └── logback/
└── src/test/java/eusyaeusya/gmg
```
## 도메인 흐름

### 1. 모임 생성

`POST /api/events` 요청으로 이벤트를 생성합니다. 서버는 선택 날짜 목록을 정렬/검증하고, 이벤트와 장소 유형을 저장한 뒤 장소 검색 이벤트를 발행합니다.

현재 날짜 모델은 연속 기간이 아니라 `selectedDates` 목록입니다. `date_start`, `date_end`는 최소/최대 선택일을 저장하는 파생 메타데이터이고, 히트맵/검증/추천 로직은 `selectedDates`를 기준으로 동작합니다.

### 2. 장소 수집

모임 생성 이후 비동기 이벤트로 카카오맵 장소 검색을 실행합니다. 검색 대상은 이벤트에 연결된 장소 유형과 카테고리 키워드이며, 같은 외부 ID를 가진 장소는 중복 제거합니다.

Google Maps API는 장소 상세 정보, 영업시간, 사진 URL 보강에 사용합니다. `local` 프로필에서는 `KakaoMapClient`가 비활성화되어 실제 카카오맵 검색을 건너뜁니다.

### 3. 참여자 입력

참여자는 이름으로 이벤트에 참여하고, 불가능한 시간과 비선호 카테고리/장소를 입력합니다. 입력 완료 상태가 된 참여자만 히트맵과 추천 계산에 반영됩니다.

### 4. 히트맵과 추천

히트맵은 선택 날짜와 시간 범위를 30분 슬롯으로 나눈 뒤, 완료된 참여자의 불가능 시간을 차감해 가능 인원과 intensity를 계산합니다.

장소 추천은 아래 기준을 함께 반영합니다.

- 이벤트 중심 좌표 기준 반경 내 장소
- 이벤트가 선택한 장소 유형
- 완료된 참여자의 비선호 카테고리/장소
- 장소 영업일과 이벤트 선택 날짜의 매칭률
- 히트맵 기반 시간대 가중치

### 5. 실시간 업데이트

참여자 입력이 커밋된 뒤 `TransactionalEventListener`가 히트맵과 추천을 다시 계산하고, `SseService`가 `/api/events/{hashUrl}/stream` 구독자에게 이벤트를 전송합니다.

SSE 응답은 프록시 환경에서 버퍼링되지 않도록 `X-Accel-Buffering: no`와 캐시 방지 헤더를 사용합니다. 서버는 10초마다 heartbeat 코멘트를 전송해 연결을 유지합니다.




