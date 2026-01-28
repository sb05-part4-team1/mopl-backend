plugins {
    `java-library`
}

dependencies {
    // base test utilities
    api(project(":shared:test-core"))
    // project modules for integration tests
    api(project(":core:domain"))
    api(project(":infrastructure:jpa"))
    // jackson
    api("com.fasterxml.jackson.core:jackson-databind")
    // spring boot testcontainers
    api("org.springframework.boot:spring-boot-testcontainers")
    // testcontainers
    api("org.testcontainers:junit-jupiter")
    api("org.testcontainers:testcontainers-mysql")
    api("org.testcontainers:testcontainers-kafka")
    api("org.testcontainers:testcontainers-elasticsearch")
    // fixture monkey
    api("com.navercorp.fixturemonkey:fixture-monkey-starter:${project.properties["fixtureMonkeyVersion"]}")
}
