plugins {
    id("java")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":shared:jackson"))

    // Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
}
