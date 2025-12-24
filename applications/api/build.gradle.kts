plugins {
    id("org.springframework.boot")
}

dependencies {

    // 내부 모듈 연결
    implementation(project(":cores:domain"))
    implementation(project(":modules:jpa"))

    // Web API
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework:spring-tx")

    // Swagger
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.4")

    // Security
    implementation("org.springframework.boot:spring-boot-starter-security")

    // JWT → Nimbus
    implementation("com.nimbusds:nimbus-jose-jwt:10.3")

    // Retry
    implementation("org.springframework.retry:spring-retry")

    // MapStruct
    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

    // TEST
    testImplementation("org.springframework.security:spring-security-test")
}
