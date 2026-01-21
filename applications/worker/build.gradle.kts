dependencies {
    // project modules
    implementation(project(":core:domain"))
    implementation(project(":infrastructure:jpa"))
    implementation(project(":infrastructure:redis"))
    implementation(project(":infrastructure:kafka"))
    implementation(project(":shared:jackson"))
    // actuator
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    // test
    testImplementation(testFixtures(project(":core:domain")))
}
