dependencies {
    // project modules
    implementation(project(":core:domain"))
    implementation(project(":infrastructure:jpa"))
    implementation(project(":infrastructure:security"))
    implementation(project(":infrastructure:redis"))
    implementation(project(":shared:dto"))
    implementation(project(":shared:jackson"))
    implementation(project(":shared:logging"))
    implementation(project(":shared:monitoring"))
    // web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${project.properties["springDocOpenApiVersion"]}")
    // uuid v7
    implementation("com.fasterxml.uuid:java-uuid-generator:${project.properties["javaUuidGeneratorVersion"]}")
    // test
    testImplementation(project(":shared:test-core"))
}
