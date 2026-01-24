dependencies {
    // project modules
    implementation(project(":core:domain"))
    implementation(project(":infrastructure:cache"))
    implementation(project(":infrastructure:jpa"))
    implementation(project(":infrastructure:kafka"))
    implementation(project(":infrastructure:mail"))
    implementation(project(":infrastructure:redis"))
    implementation(project(":infrastructure:security"))
    implementation(project(":infrastructure:storage"))
    implementation(project(":shared:jackson"))
    implementation(project(":shared:logging"))
    implementation(project(":shared:monitoring"))
    // web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${project.properties["springDocOpenApiVersion"]}")
    // test
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation(testFixtures(project(":core:domain")))
}
