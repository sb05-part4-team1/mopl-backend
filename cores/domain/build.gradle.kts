plugins {
    id("java-library")
    id("io.spring.dependency-management")
}

dependencies {

    // Validation 인터페이스는 도메인 모델 검증을 위해 필요할 수 있음
    api("org.springframework.boot:spring-boot-starter-validation")

    // 테스트는 외부 의존성 없이 JUnit5와 Mockito만 사용
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.mockito:mockito-core")
}
