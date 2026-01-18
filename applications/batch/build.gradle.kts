dependencies {
    // core
    implementation(project(":core:domain"))

    // infrastructure
    implementation(project(":infrastructure:jpa"))
    implementation(project(":infrastructure:openapi"))
    implementation(project(":infrastructure:storage"))
    implementation(project(":shared:jackson"))

    // batch
    implementation("org.springframework.boot:spring-boot-starter-batch")

    // actuator
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // test
    testImplementation(testFixtures(project(":core:domain")))
}
