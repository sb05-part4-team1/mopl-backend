plugins {
    `java-library`
}

dependencies {
    // project modules
    api(project(":core:domain"))
    api(project(":infrastructure:jpa"))
    // jackson
    api("com.fasterxml.jackson.core:jackson-databind")
    // spring boot test
    api("org.springframework.boot:spring-boot-starter-test")
    api("org.springframework.boot:spring-boot-testcontainers")
    // testcontainers
    api("org.testcontainers:junit-jupiter")
    api("org.testcontainers:mysql")
    api("org.testcontainers:kafka")
    api("org.testcontainers:elasticsearch")
    // fixture monkey
    api("com.navercorp.fixturemonkey:fixture-monkey-starter:${project.properties["fixtureMonkeyVersion"]}")
    // security test
    api("org.springframework.security:spring-security-test")
    // awaitility for async testing
    api("org.awaitility:awaitility")
}
