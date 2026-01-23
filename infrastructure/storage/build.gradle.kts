plugins {
    `java-library`
}

dependencies {
    // project modules
    implementation(project(":core:domain"))
    // spring boot
    implementation("org.springframework.boot:spring-boot-starter")
    // validation
    implementation("org.springframework.boot:spring-boot-starter-validation")
    // cache
    implementation("org.springframework.boot:spring-boot-starter-cache")
    // aws s3
    implementation(platform("software.amazon.awssdk:bom:${property("awsS3SdkVersion")}"))
    implementation("software.amazon.awssdk:s3")
}
