# MOPL Backend 테스트 전략

## 1. 현재 상태 분석

### 1.1 모듈별 테스트 현황

| 모듈 | 소스 파일 | 테스트 파일 | 커버리지 | 상태 |
|------|----------|------------|---------|------|
| core/domain | 168 | 28 | 16.7% | 개선 필요 |
| infrastructure/jpa | 108 | 46 | 42.6% | 양호 |
| applications/api | 51 | 23 | 45.1% | 양호 |
| infrastructure/security | 34 | 6 | 17.6% | 개선 필요 |
| shared/dto | 25 | 12 | 48.0% | 양호 |
| applications/websocket | 24 | 6 | 25.0% | 개선 필요 |
| infrastructure/redis | 13 | 8 | 61.5% | 양호 |
| applications/sse | 14 | 6 | 42.9% | 양호 |
| infrastructure/cache | ~20 | 2 | 10% | 개선 필요 |
| infrastructure/mail | ~15 | 1 | 6.7% | 개선 필요 |
| applications/worker | ~18 | 1 | 5.6% | 개선 필요 |
| applications/batch | ~35 | 0 | 0% | 미구현 |
| infrastructure/kafka | ~25 | 0 | 0% | 미구현 |
| infrastructure/search | ~15 | 0 | 0% | 미구현 |

**전체 통계:**
- 총 소스 파일: 566개
- 총 테스트 파일: 143개 (25.3%)
- 미테스트 모듈: 3개 (batch, kafka, search)

### 1.2 사용 중인 테스트 프레임워크

| 프레임워크 | 버전 | 용도 |
|-----------|------|------|
| JUnit Jupiter | 5.x | 테스트 프레임워크 |
| Mockito | 4.x | 모킹 라이브러리 |
| AssertJ | 3.x | 플루언트 단언문 |
| Spring Boot Test | 3.3.3 | 스프링 테스트 유틸리티 |
| Spring Security Test | 6.x | 보안 테스트 |
| Fixture Monkey | 1.1.15 | 테스트 픽스처 생성 |

---

## 2. 테스트 피라미드 전략

```
                    ┌─────────┐
                    │   E2E   │  ← 5% (핵심 시나리오)
                   ─┴─────────┴─
                  ┌─────────────┐
                  │ Integration │  ← 25% (모듈 간 통합)
                 ─┴─────────────┴─
                ┌─────────────────┐
                │      Unit       │  ← 70% (비즈니스 로직)
               ─┴─────────────────┴─
```

### 2.1 단위 테스트 (Unit Tests) - 70%

**대상:**
- Domain 서비스 (비즈니스 로직)
- 모델/엔티티 변환 로직
- 유틸리티 클래스
- Mapper 클래스

**패턴:**
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock UserRepository userRepository;
    @InjectMocks UserService userService;

    @Nested
    @DisplayName("사용자 조회")
    class FindUser {
        @Test
        @DisplayName("존재하는 사용자를 조회하면 사용자 정보를 반환한다")
        void returnsUserWhenExists() {
            // given
            var user = UserModelFixture.create();
            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

            // when
            var result = userService.findById(user.getId());

            // then
            assertThat(result).isEqualTo(user);
        }
    }
}
```

### 2.2 통합 테스트 (Integration Tests) - 25%

**대상:**
- JPA Repository 구현체
- Redis 연동
- API Controller (슬라이스 테스트)
- 보안 설정

**패턴 - Repository:**
```java
@DataJpaTest(showSql = false)
@Import({JpaConfig.class, JpaUserRepository.class})
class JpaUserRepositoryTest {
    @Autowired JpaUserRepository repository;
    @Autowired TestEntityManager em;

    @Test
    @DisplayName("사용자를 저장하고 조회할 수 있다")
    void saveAndFind() {
        // given
        var entity = UserEntityFixture.create();
        em.persist(entity);
        em.flush();
        em.clear();

        // when
        var result = repository.findById(entity.getId());

        // then
        assertThat(result).isPresent();
    }
}
```

**패턴 - Controller:**
```java
@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiControllerAdvice.class)
class UserControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean UserFacade userFacade;

    @Test
    @DisplayName("사용자 프로필을 조회할 수 있다")
    void getProfile() throws Exception {
        // given
        var response = UserResponseFixture.create();
        given(userFacade.getProfile(anyLong())).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }
}
```

### 2.3 E2E 테스트 (End-to-End Tests) - 5%

**대상:**
- 핵심 사용자 시나리오
- 인증/인가 플로우
- 결제/구독 플로우

**패턴:**
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
class AuthenticationE2ETest {
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

    @Autowired TestRestTemplate restTemplate;

    @Test
    @DisplayName("회원가입부터 로그인까지 전체 플로우")
    void fullAuthenticationFlow() {
        // 1. 회원가입
        var signupRequest = new SignupRequest("test@email.com", "password123");
        var signupResponse = restTemplate.postForEntity("/api/auth/signup", signupRequest, Void.class);
        assertThat(signupResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // 2. 로그인
        var loginRequest = new LoginRequest("test@email.com", "password123");
        var loginResponse = restTemplate.postForEntity("/api/auth/login", loginRequest, TokenResponse.class);
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody().accessToken()).isNotNull();
    }
}
```

---

## 3. 모듈별 테스트 전략

### 3.1 Core Domain (우선순위: 높음)

**목표 커버리지:** 85%+

| 컴포넌트 | 테스트 유형 | 우선순위 |
|---------|-----------|---------|
| Service | Unit | 높음 |
| Model | Unit | 중간 |
| Event | Unit | 중간 |
| Exception | Unit | 낮음 |

**핵심 테스트 대상:**
- `UserService` - 사용자 CRUD, 팔로우/언팔로우
- `PlaylistService` - 플레이리스트 관리, 구독
- `ContentService` - 콘텐츠 CRUD
- `ReviewService` - 리뷰 작성, 수정, 삭제
- `ConversationService` - DM, 대화방 관리
- `NotificationService` - 알림 생성, 조회
- `OutboxService` - 이벤트 발행

### 3.2 Applications/API (우선순위: 높음)

**목표 커버리지:** 70%+

| 컴포넌트 | 테스트 유형 | 우선순위 |
|---------|-----------|---------|
| Controller | Integration (WebMvcTest) | 높음 |
| Facade | Unit | 높음 |
| Interceptor | Unit | 중간 |

**핵심 테스트 대상:**
- `AuthController` - 인증/인가 API
- `UserController` - 사용자 API
- `PlaylistController` - 플레이리스트 API
- `ContentController` - 콘텐츠 API

### 3.3 Applications/Batch (우선순위: 매우 높음 - 미구현)

**목표 커버리지:** 60%+

| 컴포넌트 | 테스트 유형 | 우선순위 |
|---------|-----------|---------|
| Job | Integration | 높음 |
| Step | Integration | 높음 |
| Reader/Writer | Unit | 중간 |
| Scheduler | Unit | 낮음 |

**핵심 테스트 대상:**
- `OrphanCleanupJob` - 고아 데이터 정리
- `DenormalizedSyncJob` - 비정규화 데이터 동기화
- 각 Job의 정상/실패 시나리오

**패턴:**
```java
@SpringBatchTest
@SpringBootTest
class OrphanCleanupJobTest {
    @Autowired JobLauncherTestUtils jobLauncherTestUtils;

    @Test
    @DisplayName("고아 리뷰 데이터를 정리한다")
    void cleanupOrphanReviews() throws Exception {
        // given
        setupOrphanData();

        // when
        var execution = jobLauncherTestUtils.launchJob();

        // then
        assertThat(execution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        assertThat(countOrphanReviews()).isZero();
    }
}
```

### 3.4 Infrastructure/Kafka (우선순위: 매우 높음 - 미구현)

**목표 커버리지:** 60%+

| 컴포넌트 | 테스트 유형 | 우선순위 |
|---------|-----------|---------|
| Producer | Integration | 높음 |
| Consumer | Integration | 높음 |
| Serializer | Unit | 중간 |

**핵심 테스트 대상:**
- 이벤트 발행/소비
- 직렬화/역직렬화
- 에러 처리 및 재시도

**패턴:**
```java
@EmbeddedKafka(partitions = 1, topics = {"test-topic"})
@SpringBootTest
class KafkaEventPublisherTest {
    @Autowired KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired KafkaEventPublisher publisher;

    @Test
    @DisplayName("이벤트를 Kafka로 발행할 수 있다")
    void publishEvent() {
        // given
        var event = new TestEvent("data");

        // when
        publisher.publish(event);

        // then
        ConsumerRecord<String, Object> record =
            KafkaTestUtils.getSingleRecord(consumer, "test-topic");
        assertThat(record.value()).isEqualTo(event);
    }
}
```

### 3.5 Infrastructure/Search (우선순위: 높음 - 미구현)

**목표 커버리지:** 50%+

| 컴포넌트 | 테스트 유형 | 우선순위 |
|---------|-----------|---------|
| Repository | Integration | 높음 |
| IndexService | Integration | 중간 |
| SearchQuery | Unit | 중간 |

**핵심 테스트 대상:**
- 콘텐츠 인덱싱
- 검색 쿼리
- 동기화 로직

**패턴 (Testcontainers):**
```java
@Testcontainers
@SpringBootTest
class ElasticsearchContentRepositoryTest {
    @Container
    static ElasticsearchContainer elasticsearch =
        new ElasticsearchContainer("elasticsearch:8.11.0");

    @Test
    @DisplayName("콘텐츠를 인덱싱하고 검색할 수 있다")
    void indexAndSearch() {
        // given
        var content = ContentFixture.create();
        repository.index(content);

        // when
        var results = repository.search("keyword");

        // then
        assertThat(results).contains(content);
    }
}
```

### 3.6 Infrastructure/JPA (우선순위: 중간)

**목표 커버리지:** 60%+ (현재 42.6%)

| 컴포넌트 | 테스트 유형 | 우선순위 |
|---------|-----------|---------|
| Repository | Integration | 높음 |
| QueryRepository | Integration | 높음 |
| Mapper | Unit | 중간 |

**핵심 테스트 대상:**
- 커서 기반 페이지네이션
- 복잡한 쿼리 (필터링, 정렬)
- 트랜잭션 처리

### 3.7 Infrastructure/Security (우선순위: 중간)

**목표 커버리지:** 50%+ (현재 17.6%)

| 컴포넌트 | 테스트 유형 | 우선순위 |
|---------|-----------|---------|
| JwtProvider | Unit | 높음 |
| OAuth2Handler | Integration | 높음 |
| SecurityFilter | Integration | 중간 |

**핵심 테스트 대상:**
- JWT 생성/검증/갱신
- OAuth2 인증 플로우
- 권한 검사

### 3.8 Applications/WebSocket & SSE (우선순위: 중간)

**목표 커버리지:** 50%+

| 컴포넌트 | 테스트 유형 | 우선순위 |
|---------|-----------|---------|
| Controller | Integration | 높음 |
| Handler | Unit | 중간 |
| Session Manager | Unit | 중간 |

**핵심 테스트 대상:**
- 연결 수립/종료
- 메시지 전송/수신
- 세션 관리

---

## 4. 테스트 인프라 설정

### 4.1 Testcontainers 설정

```java
// 공용 컨테이너 설정 (applications/api/src/test/java)
public abstract class IntegrationTestSupport {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("mopl_test")
        .withUsername("test")
        .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }
}
```

### 4.2 테스트 픽스처 (Fixture Monkey)

```java
// 공용 픽스처 팩토리
public class FixtureFactory {
    private static final FixtureMonkey MONKEY = FixtureMonkey.builder()
        .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
        .build();

    public static <T> T create(Class<T> type) {
        return MONKEY.giveMeOne(type);
    }

    public static <T> T create(Class<T> type, Consumer<ArbitraryBuilder<T>> customizer) {
        ArbitraryBuilder<T> builder = MONKEY.giveMeBuilder(type);
        customizer.accept(builder);
        return builder.sample();
    }
}

// 도메인별 픽스처
public class UserFixture {
    public static UserModel create() {
        return FixtureFactory.create(UserModel.class, builder ->
            builder.set("email", Arbitraries.strings().withCharRange('a', 'z').ofLength(10) + "@test.com")
                   .set("status", UserStatus.ACTIVE)
        );
    }
}
```

### 4.3 테스트 유틸리티

```java
// JWT 테스트 유틸리티
public class JwtTestUtils {
    public static String createTestToken(Long userId, String role) {
        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("role", role)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 3600000))
            .signWith(TEST_SECRET_KEY)
            .compact();
    }
}

// MockMvc 테스트 유틸리티
public class MockMvcTestUtils {
    public static ResultActions performWithAuth(MockMvc mockMvc, MockHttpServletRequestBuilder request, Long userId) {
        return mockMvc.perform(request
            .header("Authorization", "Bearer " + JwtTestUtils.createTestToken(userId, "USER")));
    }
}
```

---

## 5. 구현 로드맵

### Phase 1: 기반 구축 (1주)

- [ ] Testcontainers 공용 설정 구성
- [ ] 테스트 픽스처 팩토리 구현
- [ ] 테스트 유틸리티 클래스 구현
- [ ] CI/CD 테스트 파이프라인 설정

### Phase 2: 미구현 모듈 테스트 (2주)

- [ ] **Batch 모듈 테스트**
  - [ ] OrphanCleanupJob 테스트
  - [ ] DenormalizedSyncJob 테스트
  - [ ] 스케줄러 테스트

- [ ] **Kafka 모듈 테스트**
  - [ ] EventPublisher 테스트
  - [ ] EventConsumer 테스트
  - [ ] 직렬화/역직렬화 테스트

- [ ] **Search 모듈 테스트**
  - [ ] 인덱싱 테스트
  - [ ] 검색 쿼리 테스트

### Phase 3: 핵심 도메인 테스트 강화 (2주)

- [ ] **Domain 서비스 테스트** (목표: 85%)
  - [ ] UserService 테스트 확장
  - [ ] PlaylistService 테스트 확장
  - [ ] ContentService 테스트 확장
  - [ ] ReviewService 테스트 확장

- [ ] **Security 테스트** (목표: 50%)
  - [ ] JWT 라이프사이클 테스트
  - [ ] OAuth2 플로우 테스트

### Phase 4: 통합 테스트 강화 (1주)

- [ ] API 컨트롤러 통합 테스트
- [ ] Repository 통합 테스트
- [ ] 캐시 통합 테스트

### Phase 5: E2E 테스트 (1주)

- [ ] 인증 플로우 E2E
- [ ] 핵심 사용자 시나리오 E2E
- [ ] 실시간 기능 E2E (WebSocket/SSE)

---

## 6. 테스트 품질 기준

### 6.1 커버리지 목표

| 모듈 유형 | 최소 커버리지 | 권장 커버리지 |
|----------|-------------|-------------|
| Core Domain | 70% | 85% |
| Applications | 50% | 70% |
| Infrastructure | 40% | 60% |
| Shared | 50% | 70% |

### 6.2 테스트 작성 규칙

1. **네이밍 규칙**
   - 테스트 클래스: `{대상클래스}Test`
   - 테스트 메서드: `@DisplayName`으로 한글 설명 작성

2. **구조 규칙**
   - Given-When-Then 패턴 사용
   - `@Nested` 클래스로 테스트 그룹화
   - 하나의 테스트는 하나의 동작만 검증

3. **격리 규칙**
   - 테스트 간 상태 공유 금지
   - 외부 시스템 의존성 모킹 또는 컨테이너화
   - 테스트 데이터는 테스트 내에서 생성

4. **성능 규칙**
   - 단위 테스트: 100ms 이내
   - 통합 테스트: 3초 이내
   - E2E 테스트: 30초 이내

### 6.3 리뷰 체크리스트

- [ ] 테스트가 실패할 수 있는 시나리오를 검증하는가?
- [ ] 엣지 케이스를 포함하는가?
- [ ] 테스트 이름이 검증 내용을 명확히 설명하는가?
- [ ] 불필요한 모킹이 없는가?
- [ ] 테스트가 다른 테스트에 의존하지 않는가?

---

## 7. CI/CD 통합

### 7.1 GitHub Actions 워크플로우

```yaml
# .github/workflows/test.yml
name: Test

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest

    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: test
          MYSQL_DATABASE: mopl_test
        ports:
          - 3306:3306
      redis:
        image: redis:7-alpine
        ports:
          - 6379:6379

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Run tests
        run: ./gradlew test

      - name: Generate coverage report
        run: ./gradlew jacocoTestReport

      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          file: build/reports/jacoco/aggregate/jacocoTestReport.xml
```

### 7.2 커버리지 게이트

```kotlin
// build.gradle.kts
jacocoTestCoverageVerification {
    violationRules {
        rule {
            element = "BUNDLE"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.50".toBigDecimal()
            }
        }

        rule {
            element = "CLASS"
            includes = listOf("com.mopl.domain.service.*")
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.70".toBigDecimal()
            }
        }
    }
}
```

---

## 8. 부록

### 8.1 테스트 실행 명령어

```bash
# 전체 테스트 실행
./gradlew test

# 특정 모듈 테스트
./gradlew :core:domain:test
./gradlew :applications:api:test

# 커버리지 리포트 생성
./gradlew jacocoTestReport

# 커버리지 검증
./gradlew jacocoTestCoverageVerification

# 특정 테스트 클래스 실행
./gradlew test --tests "com.mopl.domain.service.UserServiceTest"

# 특정 패턴 테스트 실행
./gradlew test --tests "*ServiceTest"
```

### 8.2 유용한 Gradle 태스크

```bash
# 테스트 + 커버리지 한번에
./gradlew check

# 병렬 테스트 (주의: 리소스 충돌 가능)
./gradlew test --parallel

# 실패한 테스트만 재실행
./gradlew test --rerun-tasks

# 테스트 리포트 위치
# build/reports/tests/test/index.html
# build/reports/jacoco/aggregate/html/index.html
```

### 8.3 참고 자료

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [Testcontainers](https://www.testcontainers.org/)
- [Fixture Monkey](https://naver.github.io/fixture-monkey/)
