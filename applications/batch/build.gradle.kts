dependencies {
    // project modules
    implementation(project(":core:domain"))
    implementation(project(":infrastructure:jpa"))
    implementation(project(":infrastructure:openapi"))
    implementation(project(":infrastructure:storage"))
    implementation(project(":infrastructure:search"))
    implementation(project(":shared:monitoring"))
    // batch
    implementation("org.springframework.boot:spring-boot-starter-batch")
    // test
    testImplementation(testFixtures(project(":core:domain")))
}
