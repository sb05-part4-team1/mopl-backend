plugins {
    id("java")
}

dependencies {
    implementation(project(":infrastructure:jpa"))

    // Jackson (필수)
    implementation("com.fasterxml.jackson.core:jackson-core")
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // Cache
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine")
}
