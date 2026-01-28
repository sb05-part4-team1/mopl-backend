dependencies {
    // project modules
    implementation(project(":core:domain"))
    implementation(project(":infrastructure:jpa"))
    implementation(project(":infrastructure:openapi"))
    implementation(project(":infrastructure:storage"))
    implementation(project(":infrastructure:search"))
    implementation(project(":shared:logging"))
    implementation(project(":shared:monitoring"))
    // web (for actuator endpoints)
    implementation("org.springframework.boot:spring-boot-starter-web")
    // batch
    implementation("org.springframework.boot:spring-boot-starter-batch")
    // test
    testImplementation(testFixtures(project(":core:domain")))
    testImplementation(project(":shared:test"))
    testImplementation("org.springframework.batch:spring-batch-test")
}
