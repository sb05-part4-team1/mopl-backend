plugins {
    id("java")
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":shared:jackson"))

    // Spring Cache
    implementation("org.springframework.boot:spring-boot-starter-cache")

    // Caffeine (Local Cache)
    implementation("com.github.ben-manes.caffeine:caffeine")
}
