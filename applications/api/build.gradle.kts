dependencies {
    // project modules
    implementation(project(":core:domain"))
    implementation(project(":infrastructure:jpa"))
    implementation(project(":infrastructure:security"))
    implementation(project(":infrastructure:storage"))
    implementation(project(":shared:jackson"))

    implementation(project(":applications:sse"))

    // web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${project.properties["springDocOpenApiVersion"]}")
    // [필수] 시큐리티 테스트 라이브러리 (user(), csrf() 사용을 위해 필요)
    testImplementation("org.springframework.security:spring-security-test")

    // test fixtures
    testImplementation(testFixtures(project(":core:domain")))
}
