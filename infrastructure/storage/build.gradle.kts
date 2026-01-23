plugins {
    `java-library`
}

dependencies {
    // spring boot
    implementation("org.springframework.boot:spring-boot-starter")
    // validation
    implementation("org.springframework.boot:spring-boot-starter-validation")
    // aws s3
    implementation(platform("software.amazon.awssdk:bom:${property("awsS3SdkVersion")}"))
    implementation("software.amazon.awssdk:s3")
}
