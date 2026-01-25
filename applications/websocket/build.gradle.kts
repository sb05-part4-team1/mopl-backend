dependencies {
    // project modules
    implementation(project(":core:domain"))
    implementation(project(":infrastructure:jpa"))
    implementation(project(":infrastructure:redis"))
    implementation(project(":infrastructure:security"))
    implementation(project(":infrastructure:storage"))
    implementation(project(":shared:dto"))
    implementation(project(":shared:monitoring"))
    // spring websocket
    implementation("org.springframework.boot:spring-boot-starter-websocket")
}
