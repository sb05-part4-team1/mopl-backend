# Docker 로컬 개발 환경

## 사전 준비

```bash
# Docker 네트워크 생성 (최초 1회)
docker network create mopl-net
```

## 서비스 구성

| 파일                             | 서비스                                 | 용도         |
|--------------------------------|-------------------------------------|------------|
| docker-compose-infra.yaml      | Redis, Kafka, Elasticsearch, Kibana | 핵심 인프라     |
| docker-compose-monitoring.yaml | Prometheus, Grafana, InfluxDB       | 모니터링       |
| docker-compose-logging.yaml    | Fluent Bit                          | 로그 수집 (S3) |

## 실행

### 인프라 (필수)

```bash
docker compose -f docker/docker-compose-infra.yaml up -d
```

### 모니터링 (선택)

```bash
docker compose -f docker/docker-compose-monitoring.yaml up -d
```

### 로깅 (선택, S3 연동 시)

```bash
# 환경변수 설정
export S3_BUCKET=mopl-logs-dev
export AWS_REGION=ap-northeast-2
export AWS_ACCESS_KEY_ID=your-access-key
export AWS_SECRET_ACCESS_KEY=your-secret-key

docker compose -f docker/docker-compose-logging.yaml up -d
```

## 접속 정보

### 인프라

| 서비스           | URL                   | 비고        |
|---------------|-----------------------|-----------|
| Redis         | localhost:6379        |           |
| RedisInsight  | http://localhost:5540 | Redis UI  |
| Kafka         | localhost:9092        | 외부 접속용    |
| Kafka UI      | http://localhost:8090 | 토픽/메시지 관리 |
| Elasticsearch | http://localhost:9200 |           |
| Kibana        | http://localhost:5601 | ES 시각화/쿼리 |

### 모니터링

| 서비스        | URL                   | 비고            |
|------------|-----------------------|---------------|
| Prometheus | http://localhost:9090 | 메트릭 수집        |
| Grafana    | http://localhost:3000 | admin / admin |
| InfluxDB   | http://localhost:8086 | k6 부하테스트용     |

### 로깅

| 서비스        | URL                   | 비고             |
|------------|-----------------------|----------------|
| Fluent Bit | http://localhost:2020 | Health/Metrics |

## UI 사용법

### RedisInsight (http://localhost:5540)

**최초 연결 설정:**
1. `+ Add Redis database` 클릭
2. 연결 정보 입력:
   - Host: `mopl-redis`
   - Port: `6379`
   - Database Alias: `mopl-local`
3. `Add Redis Database` 클릭

**주요 기능:**
- Browser: 키/값 조회, 추가, 삭제
- CLI: Redis 명령어 직접 실행
- Slowlog: 느린 쿼리 분석

### Kafka UI (http://localhost:8090)

**주요 기능:**
- Topics: 토픽 목록, 메시지 조회, 토픽 생성
- Consumers: 컨슈머 그룹 상태, 랙 확인
- Brokers: 브로커 상태 모니터링

**메시지 조회:**
1. Topics → 토픽 선택 → Messages
2. 필터링: Offset, Timestamp, Key 등

### Kibana (http://localhost:5601)

**Dev Tools (ES 쿼리 실행):**

Management → Dev Tools에서 직접 쿼리 실행

```bash
# 인덱스 목록
GET _cat/indices?v

# 데이터 조회
GET mopl-contents/_search
{
  "query": { "match_all": {} },
  "size": 10
}

# 검색 테스트
GET mopl-contents/_search
{
  "query": {
    "match": { "title": "검색어" }
  }
}

# 매핑 확인
GET mopl-contents/_mapping

# 클러스터 상태
GET _cluster/health
```

**Discover (데이터 탐색):**
1. Stack Management → Index Patterns → Create index pattern
2. 패턴: `mopl-*` 입력
3. Discover에서 데이터 검색

### Grafana (http://localhost:3000)

**로그인:** admin / admin

**데이터소스 추가:**
1. Configuration → Data Sources → Add data source
2. Prometheus 선택
3. URL: `http://prometheus:9090`
4. Save & Test

**대시보드:**
- Dashboards → Import → Grafana.com ID 입력
- 추천 대시보드:
  - Spring Boot: `12900`
  - JVM Micrometer: `4701`
  - Redis: `11835`

## 상태 확인

```bash
# 실행 중인 컨테이너 확인
docker ps

# 특정 서비스 로그 확인
docker logs mopl-kafka
docker logs mopl-elasticsearch
```

## 중지

```bash
# 개별 중지
docker compose -f docker/docker-compose-infra.yaml down
docker compose -f docker/docker-compose-monitoring.yaml down
docker compose -f docker/docker-compose-logging.yaml down

# 볼륨 포함 삭제 (데이터 초기화)
docker compose -f docker/docker-compose-infra.yaml down -v
```

## 전체 초기화

```bash
# 모든 컨테이너 중지 및 볼륨 삭제
docker compose -f docker/docker-compose-infra.yaml down -v
docker compose -f docker/docker-compose-monitoring.yaml down -v
docker compose -f docker/docker-compose-logging.yaml down -v

# 네트워크 삭제
docker network rm mopl-net
```
