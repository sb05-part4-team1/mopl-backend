dependencies {
    // project modules
    implementation(project(":core:domain"))
    implementation(project(":infrastructure:jpa"))
    implementation(project(":infrastructure:security"))
    implementation(project(":infrastructure:redis"))
    implementation(project(":shared:monitoring"))
    // web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${project.properties["springDocOpenApiVersion"]}")
}

// TODO: 배포 전 ALB 설계 후 제거
tasks.jar {
    enabled = true
}
