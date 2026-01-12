dependencies {
    // project modules
    implementation(project(":core:domain"))
    implementation(project(":infrastructure:jpa"))
    implementation(project(":infrastructure:security"))
    implementation(project(":infrastructure:storage"))
    implementation(project(":infrastructure:redis"))
    implementation(project(":shared:jackson"))
    // web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${project.properties["springDocOpenApiVersion"]}")
    // test
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation(testFixtures(project(":core:domain")))
}
