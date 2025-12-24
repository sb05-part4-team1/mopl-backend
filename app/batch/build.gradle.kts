plugins {
    id("org.springframework.boot")
}

dependencies {

    implementation(project(":modules:jpa"))
    implementation(project(":modules:redis"))
    implementation(project(":modules:kafka"))
    implementation(project(":modules:external"))

    implementation(project(":supports:monitoring"))

    // Batch
    implementation("org.springframework.boot:spring-boot-starter-batch")

    // Retry
    implementation("org.springframework.retry:spring-retry")

    // MapStruct
    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
}
