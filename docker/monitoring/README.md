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

## Grafana (http://localhost:3000)

**로그인:** admin / admin

**프로비저닝된 대시보드:**
- MOPL Overview: 서비스 상태, HTTP 요청, JVM 메트릭

**추천 Import 대시보드 (Grafana.com ID):**
- Spring Boot: `12900`
- JVM Micrometer: `4701`
- Redis: `11835`

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
