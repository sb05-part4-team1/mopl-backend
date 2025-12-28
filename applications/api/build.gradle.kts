dependencies {
    // project modules
    implementation(project(":cores:domain"))
    implementation(project(":modules:jpa"))
    implementation(project(":modules:security"))
    implementation(project(":supports:jackson"))

    // web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${project.properties["springDocOpenApiVersion"]}")
}
