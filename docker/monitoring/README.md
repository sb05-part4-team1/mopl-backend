# 모니터링 가이드

## 개요

| 영역 | 도구 | 설명 |
|------|------|------|
| 메트릭 | Prometheus + Grafana | 애플리케이션 및 인프라 메트릭 수집/시각화 |
| 로깅 | OpenSearch + Dashboards | 실시간 로그 검색 및 분석 |
| 분산 추적 | Micrometer Tracing + Zipkin | 요청 흐름 추적 |
| 알림 | Alertmanager + Slack | 장애/이상 알림 |

## 실행

```bash
docker compose -f docker/docker-compose-monitoring.yaml up -d
```

## 접속 정보

| 서비스                   | URL                   | 비고            |
|-----------------------|-----------------------|---------------|
| Prometheus            | http://localhost:9090 | 메트릭 수집        |
| Grafana               | http://localhost:3000 | admin / admin |
| Alertmanager          | http://localhost:9093 | 알림 관리         |
| Zipkin                | http://localhost:9411 | 분산 추적         |
| OpenSearch            | http://localhost:9201 | 로그 저장         |
| OpenSearch Dashboards | http://localhost:5602 | 로그 검색 UI      |

---

## 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                         Grafana                                 │
│                    http://localhost:3000                        │
└─────────────────────────────────────────────────────────────────┘
                              ↑
┌─────────────────────────────────────────────────────────────────┐
│                        Prometheus                               │
│  ┌───────┐ ┌───────┐ ┌───────┐ ┌───────┐ ┌───────┐            │
│  │  API  │ │  WS   │ │  SSE  │ │ Batch │ │Worker │            │
│  │ :8080 │ │ :8081 │ │ :8082 │ │ :8083 │ │ :8084 │            │
│  └───────┘ └───────┘ └───────┘ └───────┘ └───────┘            │
│  ┌───────┐ ┌───────┐ ┌───────┐ ┌───────┐                      │
│  │ Redis │ │ MySQL │ │ Kafka │ │  ES   │  ← Exporters         │
│  │ :9121 │ │ :9104 │ │ :9308 │ │ :9114 │                      │
│  └───────┘ └───────┘ └───────┘ └───────┘                      │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  Applications  ──→  Zipkin (분산 추적)  http://localhost:9411   │
│                ──→  OpenSearch (로그)   http://localhost:5602   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Grafana - 메트릭 시각화 (http://localhost:3000)

### 접속 및 로그인
- **URL:** http://localhost:3000
- **로그인:** admin / admin (최초 접속 시 비밀번호 변경 권장, Skip 가능)

---

### 대시보드 보기

#### 1. 프로비저닝된 대시보드 (자동 설정됨)
- 좌측 메뉴 → **Dashboards** → **MOPL Overview**
- 서비스 상태, HTTP 요청 수, JVM 메트릭 등 확인 가능

#### 2. 커뮤니티 대시보드 Import
Grafana.com에서 제공하는 대시보드를 쉽게 추가할 수 있습니다:

1. 좌측 메뉴 → **Dashboards** → **New** → **Import**
2. 대시보드 ID 입력 후 **Load**
3. Data source로 **Prometheus** 선택 → **Import**

**추천 대시보드 ID:**

| ID | 이름 | 설명 |
|----|------|------|
| `12900` | Spring Boot Statistics | Spring Boot 애플리케이션 통계 |
| `4701` | JVM (Micrometer) | JVM 상세 메트릭 (힙, GC, 스레드) |
| `11835` | Redis Dashboard | Redis 메트릭 |
| `14057` | MySQL Overview | MySQL 성능 메트릭 |
| `7589` | Kafka Exporter | Kafka 토픽/컨슈머 메트릭 |

---

### 메트릭 탐색 (Explore)

직접 쿼리를 작성하여 메트릭을 탐색할 수 있습니다:

1. 좌측 메뉴 → **Explore** (나침반 아이콘)
2. 상단에서 **Prometheus** 데이터소스 선택
3. **Metrics** 드롭다운에서 메트릭 선택 또는 직접 PromQL 입력

#### 자주 사용하는 PromQL 쿼리

**HTTP 요청 관련:**
```promql
# 초당 HTTP 요청 수 (5분 평균)
rate(http_server_requests_seconds_count[5m])

# 서비스별 초당 요청 수
sum by(application) (rate(http_server_requests_seconds_count[5m]))

# HTTP 평균 응답 시간
rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])

# 5xx 에러율 (%)
sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m]))
/ sum(rate(http_server_requests_seconds_count[5m])) * 100

# 특정 엔드포인트 응답시간 (95퍼센타일)
histogram_quantile(0.95, sum(rate(http_server_requests_seconds_bucket{uri="/api/v1/contents"}[5m])) by (le))
```

**JVM 관련:**
```promql
# 힙 메모리 사용량 (MB)
jvm_memory_used_bytes{area="heap"} / 1024 / 1024

# 힙 메모리 사용률 (%)
jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} * 100

# GC 일시정지 시간 (초)
rate(jvm_gc_pause_seconds_sum[5m])

# 활성 스레드 수
jvm_threads_live_threads
```

**커스텀 비즈니스 메트릭:**
```promql
# 캐시 히트율 (%)
sum(rate(mopl_cache_hit_total[5m]))
/ (sum(rate(mopl_cache_hit_total[5m])) + sum(rate(mopl_cache_miss_total[5m]))) * 100

# TMDB API 평균 응답시간 (ms)
rate(mopl_external_api_latency_seconds_sum{api="tmdb"}[5m])
/ rate(mopl_external_api_latency_seconds_count{api="tmdb"}[5m]) * 1000

# 활성 SSE 연결 수
mopl_sse_connections_active

# Redis 지연 시간 (ms)
rate(mopl_cache_redis_latency_seconds_sum[5m])
/ rate(mopl_cache_redis_latency_seconds_count[5m]) * 1000
```

**인프라 메트릭:**
```promql
# MySQL 커넥션 수
mysql_global_status_threads_connected

# MySQL 쿼리 초당 실행 수
rate(mysql_global_status_queries[5m])

# Kafka 컨슈머 랙
kafka_consumergroup_lag

# Redis 메모리 사용량 (MB)
redis_memory_used_bytes / 1024 / 1024

# Elasticsearch 인덱스 문서 수
elasticsearch_indices_docs
```

---

### 커스텀 대시보드 만들기

#### 새 대시보드 생성
1. 좌측 메뉴 → **Dashboards** → **New** → **New Dashboard**
2. **Add visualization** 클릭

#### 패널 추가
1. **Data source**로 **Prometheus** 선택
2. 하단 **Query** 탭에 PromQL 입력
3. 우측에서 시각화 타입 선택
4. **Apply** 클릭

#### 유용한 시각화 타입
| 타입 | 용도 | 예시 |
|------|------|------|
| **Time series** | 시간에 따른 변화 | 요청 수, 응답시간 |
| **Stat** | 단일 숫자 값 | 현재 활성 유저 수 |
| **Gauge** | 퍼센트/범위 값 | CPU 사용률, 메모리 사용률 |
| **Bar gauge** | 여러 항목 비교 | 서비스별 에러 수 |
| **Table** | 상세 데이터 | Top 10 느린 엔드포인트 |
| **Heatmap** | 분포 시각화 | 응답시간 분포 |

#### 대시보드 저장
1. 상단 💾 아이콘 또는 `Ctrl+S`
2. 이름 입력 후 **Save**

---

### 알림 확인 (Alerting)

1. 좌측 메뉴 → **Alerting** → **Alert rules**
2. Prometheus에서 정의된 알림 규칙 확인
3. 현재 발생 중인 알림은 **Alerting** → **Alert rules** → **State** 컬럼에서 `Firing` 표시

---

### 유용한 기능

#### 시간 범위 설정
- 우측 상단 시간 선택기에서 범위 선택
- 자주 사용: `Last 15 minutes`, `Last 1 hour`, `Last 24 hours`
- 커스텀 범위도 가능

#### 자동 새로고침
- 우측 상단 새로고침 아이콘 옆 드롭다운
- `5s`, `10s`, `30s`, `1m` 등 선택
- 실시간 모니터링 시 유용

#### 변수 사용 (Variables)
대시보드에서 드롭다운으로 필터링:
1. 대시보드 설정 (⚙️) → **Variables** → **Add variable**
2. 예: `application` 변수 추가 → 서비스 선택 드롭다운 생성

---

## Zipkin - 분산 추적 (http://localhost:9411)

요청이 여러 서비스를 거쳐갈 때의 흐름을 추적합니다.

**사용법:**
1. 서비스 선택 (예: mopl-api)
2. "Run Query" 클릭
3. 트레이스 클릭하여 상세 스팬 확인

**환경변수 (애플리케이션):**
```bash
TRACING_ENABLED=true                    # 추적 활성화 (기본: true)
TRACING_SAMPLING_PROBABILITY=0.1        # 샘플링 비율 (기본: 10%)
ZIPKIN_ENDPOINT=http://localhost:9411/api/v2/spans
```

---

## OpenSearch Dashboards - 로그 검색 (http://localhost:5602)

실시간 로그 검색 및 분석 도구입니다.

**초기 설정:**
1. Stack Management → Index Patterns → Create index pattern
2. 패턴 입력: `mopl-logs-*`
3. Time field: `@timestamp` 선택
4. Discover 메뉴에서 로그 검색

**유용한 쿼리:**
```
# ERROR 로그만 보기
level: ERROR

# 특정 서비스 로그
service: api AND level: ERROR

# 특정 TraceID로 검색 (Zipkin과 연계)
traceId: "abc123"

# 특정 사용자 요청 추적
userId: "user-uuid"

# 예외 포함 로그
_exists_: stacktrace
```

**환경변수 (애플리케이션):**
```bash
OPENSEARCH_URL=http://localhost:9201
```

---

## Alertmanager (http://localhost:9093)

알림 규칙 및 Slack 연동을 관리합니다.

**Slack 연동:**
```bash
export SLACK_WEBHOOK_URL="https://hooks.slack.com/services/..."
docker compose -f docker/docker-compose-monitoring.yaml up -d
```

**주요 알림 규칙:**
| 알림 | 조건 | 심각도 |
|------|------|--------|
| ServiceDown | 서비스 다운 1분 | critical |
| HighErrorRate | 5xx > 5% for 5m | warning |
| JvmMemoryHigh | heap > 85% for 5m | warning |
| KafkaConsumerLag | lag > 1000 for 5m | warning |

---

## MySQL Exporter 설정

MySQL 메트릭 수집을 위해 모니터링 사용자 생성이 필요합니다:

```sql
CREATE USER 'exporter'@'%' IDENTIFIED BY 'exporter';
GRANT PROCESS, REPLICATION CLIENT, SELECT ON *.* TO 'exporter'@'%';
FLUSH PRIVILEGES;
```

**환경변수:**
```bash
MYSQL_HOST=host.docker.internal    # MySQL 호스트 (기본값)
MYSQL_PORT=3306                    # MySQL 포트 (기본값)
MYSQL_EXPORTER_USER=exporter       # 모니터링 사용자 (기본값)
MYSQL_EXPORTER_PASSWORD=exporter   # 모니터링 비밀번호 (기본값)
```

---

## 커스텀 메트릭

애플리케이션에서 수집하는 커스텀 비즈니스 메트릭:

| 메트릭 | 설명 |
|--------|------|
| `mopl.cache.hit` | 캐시 히트 (L1/L2 태그) |
| `mopl.cache.miss` | 캐시 미스 |
| `mopl.cache.redis.latency` | Redis 지연 시간 |
| `mopl.external.api.requests` | 외부 API (TMDB/TSDB) 호출 수 |
| `mopl.external.api.latency` | 외부 API 지연 시간 |
| `mopl.external.api.errors` | 외부 API 에러 |
| `mopl.sse.connections.active` | 활성 SSE 연결 수 |
| `mopl.sse.events.sent` | 전송된 SSE 이벤트 |

---

## k6 - 부하 테스트

k6를 사용한 부하 테스트를 수행할 수 있습니다.

### 설치

```bash
# macOS
brew install k6

# Docker
docker pull grafana/k6
```

### 실행

```bash
# 기본 실행 (콘솔 출력)
k6 run k6/test.js

# 환경변수와 함께 실행
K6_TOKEN="your-jwt-token" K6_BASE_URL="http://localhost:8080" k6 run k6/test.js

# Docker로 실행
docker run --rm -i --network=host \
  -e K6_TOKEN="your-jwt-token" \
  grafana/k6 run - < k6/test.js
```

### 결과를 Prometheus로 전송

Prometheus Remote Write를 사용하여 k6 메트릭을 Grafana에서 조회할 수 있습니다:

```bash
# Prometheus Remote Write 활성화
K6_PROMETHEUS_RW_SERVER_URL=http://localhost:9090/api/v1/write \
k6 run --out experimental-prometheus-rw k6/test.js
```

### 결과를 JSON으로 저장

```bash
k6 run --out json=results.json k6/test.js
```

### 테스트 시나리오 설정

`k6/test.js`에서 시나리오 수정:

```javascript
export const options = {
    scenarios: {
        // 스파이크 테스트
        spike_test: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '10s', target: 100 },  // 급격히 증가
                { duration: '1m', target: 100 },   // 유지
                { duration: '10s', target: 0 },    // 급격히 감소
            ],
        },
        // 스트레스 테스트
        stress_test: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '2m', target: 50 },
                { duration: '5m', target: 50 },
                { duration: '2m', target: 100 },
                { duration: '5m', target: 100 },
                { duration: '2m', target: 0 },
            ],
        },
    },
};
```

### 주요 메트릭

| 메트릭 | 설명 |
|--------|------|
| `http_req_duration` | HTTP 요청 응답 시간 |
| `http_req_failed` | 실패한 HTTP 요청 비율 |
| `http_reqs` | 총 HTTP 요청 수 |
| `vus` | 현재 가상 사용자 수 |
| `iterations` | 완료된 테스트 반복 횟수 |

---

## 문제 해결

### Prometheus 타겟이 DOWN으로 표시됨
1. 애플리케이션 실행 중인지 확인
2. actuator 엔드포인트 확인: `curl http://localhost:8080/actuator/prometheus`
3. Docker 네트워크 확인

### OpenSearch에 로그가 안 보임
1. OpenSearch 상태 확인: `curl http://localhost:9201/_cluster/health`
2. 인덱스 확인: `curl http://localhost:9201/_cat/indices`
3. 애플리케이션 프로파일 확인 (local/dev만 OpenSearch 사용)

### Zipkin에 트레이스가 안 보임
1. 샘플링 비율 확인 (`TRACING_SAMPLING_PROBABILITY`)
2. Zipkin 상태 확인: `curl http://localhost:9411/health`
3. 애플리케이션 로그에서 Zipkin 연결 에러 확인
