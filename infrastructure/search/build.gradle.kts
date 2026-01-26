plugins {
    `java-library`
}

dependencies {
    // project modules
    implementation(project(":core:domain"))
    implementation(project(":shared:jackson"))
    // elasticsearch
    api("org.springframework.boot:spring-boot-starter-data-elasticsearch")
    // test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
