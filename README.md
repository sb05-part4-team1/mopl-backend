# 모플 (Mopl) Backend

<img src="./applications/api/src/main/resources/static/favicon.svg" alt="Mopl Logo" width="48" height="48">

[![codecov](https://codecov.io/gh/sb05-part4-team1/mopl-backend/graph/badge.svg)](https://codecov.io/gh/sb05-part4-team1/mopl-backend)
[![Test](https://github.com/sb05-part4-team1/mopl-backend/actions/workflows/test.yaml/badge.svg)](https://github.com/sb05-part4-team1/mopl-backend/actions/workflows/test.yaml)

**실시간 같이 보기 기능과 큐레이팅을 제공하는 소셜 서비스**

---

## 프로젝트 소개

모플은 사용자들이 콘텐츠를 함께 시청하고, 플레이리스트를 큐레이팅하며, 리뷰를 공유할 수 있는 소셜 플랫폼입니다.

### 주요 기능

- **실시간 같이 보기 (Watching Session)**: WebSocket/SSE 기반 실시간 동시 시청 세션
- **플레이리스트 큐레이팅**: 콘텐츠 플레이리스트 생성 및 구독
- **리뷰 시스템**: 콘텐츠에 대한 리뷰 작성 및 공유
- **소셜 기능**: 팔로우, 알림, 1:1 대화(DM)
- **콘텐츠 검색**: Elasticsearch 기반 검색

---

## 아키텍처

### 멀티 모듈 구조

```
mopl-backend/
├── applications/           # 실행 가능한 애플리케이션
│   ├── api/               # REST API 서버
│   ├── batch/             # Spring Batch 작업
│   ├── sse/               # Server-Sent Events 서버
│   ├── websocket/         # WebSocket 서버
│   └── worker/            # Kafka Consumer 워커
│
├── core/
│   └── domain/            # 도메인 모델 및 서비스
│
├── infrastructure/        # 외부 시스템 연동
│   ├── cache/             # 캐시 추상화
│   ├── jpa/               # JPA 엔티티 및 레포지토리
│   ├── kafka/             # Kafka Producer/Consumer
│   ├── mail/              # 이메일 발송
│   ├── openapi/           # Swagger/OpenAPI
│   ├── redis/             # Redis 연동
│   ├── search/            # Elasticsearch 연동
│   ├── security/          # Spring Security
│   └── storage/           # 파일 스토리지 (S3)
│
└── shared/                # 공통 모듈
    ├── dto/               # 공유 DTO
    ├── jackson/           # JSON 직렬화 설정
    ├── logging/           # 로깅 설정
    └── monitoring/        # 메트릭/트레이싱
```

### 도메인 모델

| 도메인 | 설명 |
|--------|------|
| User | 사용자 계정 및 프로필 |
| Content | 영상/콘텐츠 정보 |
| Playlist | 플레이리스트 및 구독 |
| Review | 콘텐츠 리뷰 |
| WatchingSession | 실시간 같이 보기 세션 |
| Follow | 팔로우 관계 |
| Notification | 알림 |
| Conversation | 1:1 대화 |
| Tag/Genre | 태그 및 장르 분류 |

---

## Tech Stack

### Backend
<div>
  <img src="https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white">
  <img src="https://img.shields.io/badge/Spring_Boot-3.3.3-6DB33F?logo=springboot&logoColor=white">
  <img src="https://img.shields.io/badge/Spring_Batch-5.x-6DB33F?logo=spring&logoColor=white">
  <img src="https://img.shields.io/badge/Spring_Security-6.x-6DB33F?logo=springsecurity&logoColor=white">
  <img src="https://img.shields.io/badge/Gradle-8.12-02303A?logo=gradle&logoColor=white">
</div>

### Database & Cache
<div>
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white">
  <img src="https://img.shields.io/badge/Redis-7.x-DC382D?logo=redis&logoColor=white">
  <img src="https://img.shields.io/badge/Elasticsearch-8.x-005571?logo=elasticsearch&logoColor=white">
</div>

### Messaging & Realtime
<div>
  <img src="https://img.shields.io/badge/Apache_Kafka-3.x-231F20?logo=apachekafka&logoColor=white">
  <img src="https://img.shields.io/badge/WebSocket-STOMP-010101?logo=websocket&logoColor=white">
  <img src="https://img.shields.io/badge/SSE-Server_Sent_Events-FF6600?logo=html5&logoColor=white">
</div>

### Monitoring & Logging
<div>
  <img src="https://img.shields.io/badge/Prometheus-E6522C?logo=prometheus&logoColor=white">
  <img src="https://img.shields.io/badge/Grafana-F46800?logo=grafana&logoColor=white">
  <img src="https://img.shields.io/badge/Zipkin-FE7139?logo=zipkin&logoColor=white">
  <img src="https://img.shields.io/badge/OpenSearch-005EB8?logo=opensearch&logoColor=white">
</div>

### Infrastructure & DevOps
<div>
  <img src="https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white">
  <img src="https://img.shields.io/badge/AWS_S3-569A31?logo=amazons3&logoColor=white">
  <img src="https://img.shields.io/badge/GitHub_Actions-2088FF?logo=githubactions&logoColor=white">
</div>

### Testing
<div>
  <img src="https://img.shields.io/badge/JUnit5-25A162?logo=junit5&logoColor=white">
  <img src="https://img.shields.io/badge/Mockito-78A641?logo=mockito&logoColor=white">
  <img src="https://img.shields.io/badge/k6-7D64FF?logo=k6&logoColor=white">
</div>

---

## 시작하기

### 사전 요구사항

- Java 21
- Docker & Docker Compose
- Gradle 8.12+

### 인프라 실행

```bash
# 인프라 (MySQL, Redis, Kafka, Elasticsearch)
docker compose -f docker/docker-compose-infra.yaml up -d

# 모니터링 (Prometheus, Grafana, Zipkin)
docker compose -f docker/docker-compose-monitoring.yaml up -d

# 로깅 (OpenSearch, Fluent Bit)
docker compose -f docker/docker-compose-logging.yaml up -d
```

### 애플리케이션 빌드 및 실행

```bash
# 빌드
./gradlew build

# API 서버 실행
./gradlew :applications:api:bootRun

# WebSocket 서버 실행
./gradlew :applications:websocket:bootRun

# SSE 서버 실행
./gradlew :applications:sse:bootRun

# Worker 실행
./gradlew :applications:worker:bootRun

# Batch 실행
./gradlew :applications:batch:bootRun
```

### 테스트

```bash
# 전체 테스트
./gradlew test

# 커버리지 리포트
./gradlew jacocoTestReport

# 부하 테스트 (k6)
k6 run k6/test.js
```

---

## API 문서

API 서버 실행 후 Swagger UI에서 확인:

```
http://localhost:8080/swagger-ui.html
```

---

## 모니터링

| 서비스 | URL | 용도 |
|--------|-----|------|
| Grafana | http://localhost:3000 | 메트릭 대시보드 |
| Prometheus | http://localhost:9090 | 메트릭 수집 |
| Zipkin | http://localhost:9411 | 분산 트레이싱 |
| OpenSearch Dashboards | http://localhost:5601 | 로그 분석 |

---

## 코드 품질

- **Checkstyle**: Google Java Style Guide
- **Spotless**: Eclipse 포매터
- **JaCoCo**: 테스트 커버리지
