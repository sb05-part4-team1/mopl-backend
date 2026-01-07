plugins {
    `java-library`
}

dependencies {
    // project modules
    implementation(project(":core:domain"))
    implementation(project(":shared:jackson"))
    // redis
    api("org.springframework.boot:spring-boot-starter-data-redis")
}
