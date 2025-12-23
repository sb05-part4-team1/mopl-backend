plugins {
    id("java")
}

dependencies {

    // WebClient
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Retry
    implementation("org.springframework.retry:spring-retry")

    // MapStruct
    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")

    // AWS S3 SDK
    implementation("software.amazon.awssdk:s3:2.31.7")
}