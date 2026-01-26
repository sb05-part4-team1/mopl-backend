dependencies {
    // core
    implementation(project(":core:domain"))

    // infrastructure
    implementation(project(":infrastructure:jpa"))
    implementation(project(":infrastructure:openapi"))
    implementation(project(":infrastructure:storage"))
    implementation(project(":shared:jackson"))
    implementation (project(":shared:monitoring"))

    // batch
    implementation("org.springframework.boot:spring-boot-starter-batch")

    // actuator
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // redis (오류해결땜에)
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // test
    testImplementation(testFixtures(project(":core:domain")))

    // 👇 이 줄을 추가해주세요!
    implementation ("org.springframework.boot:spring-boot-starter-web")
    implementation ("org.springframework.boot:spring-boot-starter-actuator")
    implementation ("io.micrometer:micrometer-registry-prometheus")


}
