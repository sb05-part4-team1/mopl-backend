dependencies {
    // core
    implementation(project(":core:domain"))

    // infrastructure
    implementation(project(":infrastructure:jpa"))
    implementation(project(":infrastructure:openapi"))
    implementation(project(":infrastructure:storage"))
    implementation(project(":infrastructure:search"))
    implementation (project(":shared:monitoring"))

    // batch
    implementation("org.springframework.boot:spring-boot-starter-batch")

    // actuator
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // test
    testImplementation(testFixtures(project(":core:domain")))

    // 모니터링 때문에 추가함
    implementation ("org.springframework.boot:spring-boot-starter-web")
    implementation ("org.springframework.boot:spring-boot-starter-actuator")
    implementation ("io.micrometer:micrometer-registry-prometheus")


}
