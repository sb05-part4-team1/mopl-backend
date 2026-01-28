plugins {
    `java-library`
}

dependencies {
    // spring boot test (includes JUnit5, Mockito, AssertJ)
    api("org.springframework.boot:spring-boot-starter-test")
    // security test
    api("org.springframework.security:spring-security-test")
    // awaitility for async testing
    api("org.awaitility:awaitility")
}
