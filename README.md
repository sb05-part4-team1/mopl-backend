# 모플 - 실시간 같이 보기 기능과 큐레이팅을 제공하는 소셜 서비스
![favicon](./applications/api/src/main/resources/static/favicon.svg)

# **🔍** 프로젝트 개요

모플(mopl)은 영화, 드라마, 스포츠 등 다양한 콘텐츠를 **큐레이팅하고 공유하며**, **실시간 같이 보기 기능**까지 제공하는 **소셜 서비스**로,
사용자들은 **자신만의 플레이리스트**를 만들고, **다른 사용자와 소통하며 콘텐츠 경험을 확장**할 수  있는 서비스

- **프로젝트 기간**: 2025.12.18 ~ 2026.01.29
- **목표**: 백엔드에 멀티모듈 적용
- **주요 특징:**
    - PostgreSQL + Spring Data JPA 기반 안정적 데이터 관리
    - Spring Batch를 이용한 클린업 작업
    - WebClient를 이용한 OpenAPI 연동
    - CI/CD를 이용한 자동 배포 구현
    - Docker 컨테이너 기반 멀티 모듈 구성
    - 프로젝트와 관련된 자료 -> https://www.notion.so/2cc7e5d24050801e930adfde439f57b3

---

# 👥 팀원 소개

| 팀장        | 팀원                                   | 팀원                                      | 팀원                                       | 팀원                                      |
|-----------|--------------------------------------|-----------------------------------------|------------------------------------------|-----------------------------------------|
| [**박지석**] | [**박지성**] | [**임재혁**] | [**김수연**] | [**류승민**] |

---

# 📌주요 기능

| 영역     | 기능                                                          |
|--------|-------------------------------------------------------------|
| 사용자    | 회원가입 · 로그인 · 수정 · 상세 조회 · 비밀번호 변경 · 역할 수정 · 프로필 변경          |
| 콘텐츠    | 목록 조회 · 콘텐츠 업로드 · 콘텐츠 상세 조회 · 삭제 · 수정                       |
| 리뷰     | 등록 · 수정 · 삭제 · 목록 조회                                        |
| 알림     | 등록 · 읽음 처리 · 목록 조회                                          |
| 플레이리스트 | 생성 · 구독 · 구독 취소 · 콘텐츠 추가 · 콘텐츠 삭제 · 목록 조회 · 상세 조회 · 삭제 · 수정 |
| 팔로우    | 팔로우 · 팔로우 여부 조회 · 팔로워 수 조회 · 팔로우 취소                         |
| 대화     | 등록 · 읽음 처리 · 대화 조회 · 대화 목록 조회 · DM 목록 조회 · 특정 사용자와의 대화 조회   |
| 시청 세션  | 시청 세션 조회 · 시청 세션 목록 조회                                      |



---

# **🧩 팀원별 담당 기능**

### 박지석

- 

### 박지성

- 

### 임재혁

- 

### 김수연

- 

### 류승민

- Conversation API
- swagger 정리

---


# 협업 방식

- Jira를 사용한 일정 관리
- Discord를 사용해 회의 진행
- PR은 다른 팀원 1명 이상의 코드리뷰를 받아 Merge
- 노션을 통한 의사소통,회의록 및 문서화 진행
- Slack을 활용하여 팀원 간 작업 진행 상황과 이슈를 실시간으로 공유

---


# **📁** 프로젝트 구조

```markdown
mopl-backend/
|--.github/
|--.gradle/
|--.idea/
|--.mopl
|   |--.logs/
|   |--.storage/
|
|--app/
|   |--api/
|   |   |--build/
|   |
|   |--batch/
|   |   |--build/
|   |
|   |--build/
|       |--spotless/
|
|--applications/
|   |--api/
|   |   |--.mopl/
|   |   |   |--.logs/
|   |   |   |--build/
|   |   |   |--src/
|   |   |   |--build.gradle.kts
|   |   |--build/
|   |   |--src/
|   |   |   |--main/
|   |   |   |   |--java/
|   |   |   |   |   |--com.mopl.domain/
|   |   |   |   |   |   |--exception/
|   |   |   |   |   |   |   |--auth/
|   |   |   |   |   |   |   |--content/
|   |   |   |   |   |   |   |--conversation/
|   |   |   |   |   |   |   |--follow/
|   |   |   |   |   |   |   |--notification/
|   |   |   |   |   |   |   |--playlist/
|   |   |   |   |   |   |   |--review/
|   |   |   |   |   |   |   |--tag/
|   |   |   |   |   |   |   |--user/
|   |   |   |   |   |   |   |--watchingsession/
|   |   |   |   |   |   |   |--ApiErrorCode
|   |   |   |   |   |   |   |--ErrorCode
|   |   |   |   |   |   |   |--ErrorResponse
|   |   |   |   |   |   |   |--InternalServerException
|   |   |   |   |   |   |   |--MoplException
|   |   |   |   |   |   |--repositoy/
|   |   |   |   |   |   |   |--content/
|   |   |   |   |   |   |   |--conversation/
|   |   |   |   |   |   |   |--follow/
|   |   |   |   |   |   |   |--league/
|   |   |   |   |   |   |   |--notification/
|   |   |   |   |   |   |   |--playlist/
|   |   |   |   |   |   |   |--review/
|   |   |   |   |   |   |   |--tag/
|   |   |   |   |   |   |   |--user/
|   |   |   |   |   |   |   |--watchingsession/
|   |   |   |   |   |   |--service/
|   |   |   |   |   |   |   |--content/
|   |   |   |   |   |   |   |--conversation/
|   |   |   |   |   |   |   |--follow/
|   |   |   |   |   |   |   |--notification/
|   |   |   |   |   |   |   |--playlist/
|   |   |   |   |   |   |   |--review/
|   |   |   |   |   |   |   |--tag/
|   |   |   |   |   |   |   |--user/
|   |   |   |   |   |   |   |--watchingsession/
|   |   |   |   |   |   |--support/
|   |   |   |   |   |   |   |--cache/
|   |   |   |   |   |   |   |--cursor/
|   |   |   |   |   |   |   |--redis/
|   |   |   |--test/
|   |   |   |--testFixtures/
|   |   |   |--build.gradle.kts
|   |--batch
|   |   |--build/
|   |   |--src/
|   |   |   |--main/
|   |   |   |   |--java/
|   |   |   |   |   |--com.mopl.batch/
|   |   |   |   |   |   |--config
|   |   |   |   |   |   |--tmdb
|   |   |   |   |   |   |   |--initializer/
|   |   |   |   |   |   |   |--job/
|   |   |   |   |   |   |   |--scheduler/
|   |   |   |   |   |   |   |--service/
|   |   |   |   |   |   |--tsdb
|   |   |   |   |   |   |   |--initializer/
|   |   |   |   |   |   |   |--job/
|   |   |   |   |   |   |   |--scheduler/
|   |   |   |   |   |   |   |--service/
|   |   |   |   |   |   |--BatchApplication
|   |   |   |   |--resources/
|   |   |   |   |   |--application.yaml
|   |   |--build.gradle.kts
|   |--sse
|   |   |--build
|   |   |--src
|   |   |   |--main
|   |   |   |   |--java
|   |   |   |   |   |--com.mopl.sse
|   |   |   |   |   |   |--com.mopl.sse/
|   |   |   |   |   |   |   |--appication/
|   |   |   |   |   |   |   |--interfaces.api/
|   |   |   |   |   |   |   |--repository/
|   |   |   |   |   |   |   |--service/
|   |   |--build.gradle.kts
|   |--websocket
|   |   |--build
|   |   |--src
|   |   |   |--main/
|   |   |   |   |--java
|   |   |   |   |   |--com.mopl.websocket/
|   |   |   |   |   |   |--application/
|   |   |   |   |   |   |   |--content/
|   |   |   |   |   |   |   |--conversation/
|   |   |   |   |   |   |--config/
|   |   |   |   |   |   |--interfaces.api/
|   |   |   |   |   |   |   |--content/
|   |   |   |   |   |   |   |--conversation/
|   |   |   |   |   |   |--monitoring/
|   |   |   |   |   |   |--repository/
|   |   |   |   |   |   |--service.content/
|   |   |   |   |   |   |--WebSocketApplication
|   |   |   |   |--resources/
|   |   |   |   |   |--application.yaml
|   |   |--build.gradle.kts
|--config/
|   |--checkstyle/
|   |   |--google_checks.xml
|   |--eclipse/
|   |   |--eclipse-java-formatter.xml
|--core/
|   |--domain/
|   |   |--src/
|   |   |   |--main/
|   |   |   |   |--java/
|   |   |   |   |   |--com.mopl.domain/
|   |   |   |   |   |   |--exception/
|   |   |   |   |   |   |   |--auth/
|   |   |   |   |   |   |   |--content/
|   |   |   |   |   |   |   |--conversation/
|   |   |   |   |   |   |   |--follow/
|   |   |   |   |   |   |   |--notification/
|   |   |   |   |   |   |   |--playlist/
|   |   |   |   |   |   |   |--review/
|   |   |   |   |   |   |   |--tag/
|   |   |   |   |   |   |   |--user/
|   |   |   |   |   |   |   |--watchingsession/
|   |   |   |   |   |   |   |--ApiErrorCode
|   |   |   |   |   |   |   |--ErrorCode
|   |   |   |   |   |   |   |--ErrorResponse
|   |   |   |   |   |   |   |--InternalServerException
|   |   |   |   |   |   |   |--MoplException
|   |   |   |   |   |   |--model/
|   |   |   |   |   |   |   |--base/
|   |   |   |   |   |   |   |--content/
|   |   |   |   |   |   |   |--conversation/
|   |   |   |   |   |   |   |--follow/
|   |   |   |   |   |   |   |--league/
|   |   |   |   |   |   |   |--notification/
|   |   |   |   |   |   |   |--playlist/
|   |   |   |   |   |   |   |--review/
|   |   |   |   |   |   |   |--tag/
|   |   |   |   |   |   |   |--user/
|   |   |   |   |   |   |   |--watchingsession/
|   |   |   |   |   |   |--repository/
|   |   |   |   |   |   |   |--content/
|   |   |   |   |   |   |   |--conversation/
|   |   |   |   |   |   |   |--follow/
|   |   |   |   |   |   |   |--league/
|   |   |   |   |   |   |   |--notification/
|   |   |   |   |   |   |   |--playlist/
|   |   |   |   |   |   |   |--review/
|   |   |   |   |   |   |   |--tag/
|   |   |   |   |   |   |   |--user/
|   |   |   |   |   |   |   |--watchingsession/
|   |   |   |   |   |   |--service/
|   |   |   |   |   |   |   |--content/
|   |   |   |   |   |   |   |--conversation/
|   |   |   |   |   |   |   |--follow/
|   |   |   |   |   |   |   |--notification/
|   |   |   |   |   |   |   |--playlist/
|   |   |   |   |   |   |   |--review/
|   |   |   |   |   |   |   |--tag/
|   |   |   |   |   |   |   |--user/
|   |   |   |   |   |   |   |--watchingsession/
|   |   |   |   |   |   |--support/
|   |   |   |   |   |   |   |--cache/
|   |   |   |   |   |   |   |--cursor/
|   |   |   |   |   |   |   |--redis/
|   |   |   |--test/
|   |   |   |--testFixtures/
|   |   |--build.gradle.kts
|--docker/
|--infrastructure/
|   |--cache/
|   |   |--src/
|   |   |   |--main/
|   |   |   |   |--java/
|   |   |   |   |   |--com.mopl.cache/
|   |   |   |   |   |   |--config/
|   |   |   |   |--resources/
|   |   |   |   |   |--cache.yaml
|   |   |   |--test/
|   |   |--build.gradle.kts
|   |--jpa/
|   |   |--src/
|   |   |   |--main/
|   |   |   |   |--java/
|   |   |   |   |   |--com.mopl.jpa/
|   |   |   |   |   |   |--config/
|   |   |   |   |   |   |--entity/
|   |   |   |   |   |   |   |--base/
|   |   |   |   |   |   |   |--content/
|   |   |   |   |   |   |   |--conversation/
|   |   |   |   |   |   |   |--follow/
|   |   |   |   |   |   |   |--league/
|   |   |   |   |   |   |   |--notification/
|   |   |   |   |   |   |   |--playlist/
|   |   |   |   |   |   |   |--review/
|   |   |   |   |   |   |   |--tag/
|   |   |   |   |   |   |   |--user/
|   |   |   |   |   |   |--repository/
|   |   |   |   |   |   |   |--content/
|   |   |   |   |   |   |   |--conversation/
|   |   |   |   |   |   |   |--follow/
|   |   |   |   |   |   |   |--league/
|   |   |   |   |   |   |   |--notification/
|   |   |   |   |   |   |   |--playlist/
|   |   |   |   |   |   |   |--review/
|   |   |   |   |   |   |   |--tag/
|   |   |   |   |   |   |   |--user/
|   |   |   |   |   |   |   |--watchingsession/
|   |   |   |   |   |   |--support.cursor/
|   |   |   |   |--resources/
|   |   |   |--test/
|   |   |--build.gradle.kts
|   |--kafka/
|   |   |--src/
|   |   |   |--main/
|   |   |   |   |--java/
|   |   |   |   |   |--com/
|   |   |   |   |   |   |--mopl/
|   |   |   |   |   |   |   |--kafka/
|   |   |   |   |   |   |   |   |--config/
|   |   |   |   |   |   |   |   |   |--kafkaConfig
|   |   |--build.gradle.kts
|   |--mail/
|   |   |--src/
|   |   |   |--main/
|   |   |   |   |--java/
|   |   |   |   |   |--com.mopl.mail.service/
|   |   |   |   |--resources/
|   |   |   |   |   |--mail.yaml
|   |   |   |--test/
|   |   |--build.gradle.kts
|   |--openapi/
|   |   |--src/
|   |   |   |--main/
|   |   |   |   |--java/
|   |   |   |   |   |--com.mopl.external/
|   |   |   |   |   |   |--tmdb/
|   |   |   |   |   |   |   |--client/
|   |   |   |   |   |   |   |--config/
|   |   |   |   |   |   |   |--exception/
|   |   |   |   |   |   |   |--model/
|   |   |   |   |   |   |   |--properties/
|   |   |   |   |   |   |--tsdb/
|   |   |   |   |   |   |   |--client/
|   |   |   |   |   |   |   |--config/
|   |   |   |   |   |   |   |--exception/
|   |   |   |   |   |   |   |--model/
|   |   |   |   |   |   |   |--properties/
|   |   |   |   |--resources/
|   |   |   |   |   |--openapi/yaml
|   |   |--build.gradle.kts
|   |--redis/
|   |   |--src/
|   |   |   |--main/
|   |   |   |   |--java/
|   |   |   |   |   |--com.mopl.redis/
|   |   |   |   |   |   |--config/
|   |   |   |   |   |   |--repository/
|   |   |   |   |   |   |   |--playlist/
|   |   |   |   |   |   |   |--user/
|   |   |   |   |--resources/
|   |   |   |--test/
|   |   |--build.gradle.kts
|   |--security/
|   |   |--src/
|   |   |   |--main/
|   |   |   |   |--java/
|   |   |   |   |   |--com.mopl.security/
|   |   |   |   |   |   |--authentication/
|   |   |   |   |   |   |   |--handler/
|   |   |   |   |   |   |   |--TemporaryPasswordAuthenticationProvider
|   |   |   |   |   |   |--config/
|   |   |   |   |   |   |--csrf/
|   |   |   |   |   |   |--exception/
|   |   |   |   |   |   |--jwt/
|   |   |   |   |   |   |   |--dto/
|   |   |   |   |   |   |   |--filter/
|   |   |   |   |   |   |   |--porovider/
|   |   |   |   |   |   |   |--registry/
|   |   |   |   |   |   |   |--service/
|   |   |   |   |   |   |--oauth2/
|   |   |   |   |   |   |   |--handler/
|   |   |   |   |   |   |   |--userinfo/
|   |   |   |   |   |   |   |--CustomOAuth2UserSErvice
|   |   |   |   |   |   |   |--OAuth2UserPrincipal
|   |   |   |   |   |   |--userdetails/
|   |   |   |   |--resources/
|   |   |   |   |   |--META-INF.spring/
|   |   |   |   |   |--security.yaml
|   |   |--build.gradle.kts
|   |--storage/
|   |   |--src/
|   |   |   |--main/
|   |   |   |   |--java/
|   |   |   |   |   |--com.mopl.storage/
|   |   |   |   |   |   |--config/
|   |   |   |   |   |   |--provider/
|   |   |   |   |--resources/
|   |   |   |   |   |--storage.yaml
|   |   |   |--test/
|   |   |--build.gradle.kts
|--shared/
|   |--jackson/
|   |   |--src/
|   |   |   |--main/
|   |   |   |   |--java/
|   |   |   |   |   |--com.mopl.jackson.config/ 
|   |   |   |   |--resources/
|   |   |   |   |   |--META-INF.spring/
|   |   |--build.gradle.kts
|   |--logging/
|   |   |--src/
|   |   |   |--main/
|   |   |   |   |--java/
|   |   |   |   |   |--com.mopl.logging/
|   |   |   |   |   |   |--config/
|   |   |   |   |   |   |--mdc/
|   |   |   |   |--resources/
|   |   |   |   |   |--META-INF.spring/
|   |   |   |--test/
|   |   |--build.gradle.kts
|   |--monitoring/
|   |   |--src/
|   |   |   |--main/
|   |   |   |   |--java/
|   |   |   |   |   |--com.mopl.shared.monitoring/
|   |   |--build.gradle.kts
|--.env
|--README.md
|--.gitignore
|--build.gradle.kts
|--settings.gradle.kts

```
---

