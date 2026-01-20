dependencies {
    // project modules
    implementation(project(":core:domain"))
    implementation(project(":infrastructure:security"))
    implementation (project(":shared:monitoring"))
    // web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${project.properties["springDocOpenApiVersion"]}")
}

tasks.jar {
    enabled = true
}
