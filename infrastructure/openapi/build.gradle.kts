plugins {
    `java-library`
}

dependencies {
    // WebClient
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    // Retry
    implementation("org.springframework.retry:spring-retry")
    // MapStruct
    implementation("org.mapstruct:mapstruct:${project.properties["mapstructVersion"]}")
    annotationProcessor("org.mapstruct:mapstruct-processor:${project.properties["mapstructVersion"]}")
    // AWS S3 SDK
    implementation(platform("software.amazon.awssdk:bom:${property("awsS3SdkVersion")}"))
    implementation("software.amazon.awssdk:s3")
}
