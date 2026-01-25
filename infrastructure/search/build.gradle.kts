plugins {
    `java-library`
}

dependencies {
    implementation(project(":core:domain"))

    // Elasticsearch
    api("org.springframework.boot:spring-boot-starter-data-elasticsearch")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
