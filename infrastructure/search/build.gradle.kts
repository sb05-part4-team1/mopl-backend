plugins {
    `java-library`
}

dependencies {
    // project modules
    implementation(project(":core:domain"))
    implementation(project(":shared:jackson"))
    implementation(project(":shared:logging"))
    // elasticsearch
    api("org.springframework.boot:spring-boot-starter-data-elasticsearch")
}
