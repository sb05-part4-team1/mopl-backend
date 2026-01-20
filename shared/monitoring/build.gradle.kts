plugins {
    id("java-library")
}

dependencies {
    // Spring Boot 3.3.3 기준으로 micrometer 버전까지 전부 고정
    api(platform("org.springframework.boot:spring-boot-dependencies:3.3.3"))
    api("org.springframework.boot:spring-boot-starter-actuator")
    api("io.micrometer:micrometer-registry-prometheus")


}
