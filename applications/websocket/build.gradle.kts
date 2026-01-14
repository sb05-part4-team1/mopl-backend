dependencies {
    implementation(project(":core:domain"))
    implementation(project(":applications:api"))
    implementation(project(":infrastructure:security"))

    implementation("org.springframework.boot:spring-boot-starter-websocket")
}
