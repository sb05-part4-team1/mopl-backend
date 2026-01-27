plugins {
    `java-library`
}

dependencies {
    // project modules
    implementation(project(":shared:logging"))
    // WebClient
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    // Retry
    implementation("org.springframework.retry:spring-retry")
    // AWS S3 SDK
    implementation(platform("software.amazon.awssdk:bom:${property("awsS3SdkVersion")}"))
    implementation("software.amazon.awssdk:s3")
}
