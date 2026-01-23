dependencies {
    // project modules
    implementation(project(":core:domain"))
    implementation(project(":applications:api"))
    implementation(project(":infrastructure:security"))
    implementation(project(":infrastructure:redis"))
    implementation(project(":infrastructure:cache"))
    implementation(project(":shared:monitoring"))
    // spring websocket
    implementation("org.springframework.boot:spring-boot-starter-websocket")
}
