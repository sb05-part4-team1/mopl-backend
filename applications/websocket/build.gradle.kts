dependencies {
    implementation(project(":core:domain"))
    implementation(project(":applications:api"))
    implementation(project(":infrastructure:security"))
    implementation(project(":infrastructure:redis"))
    implementation(project(":infrastructure:cache"))

    implementation("org.springframework.boot:spring-boot-starter-websocket")
}
