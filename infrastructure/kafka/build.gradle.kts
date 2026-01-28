plugins {
    `java-library`
}

dependencies {
    // project modules
    implementation(project(":core:domain"))
    implementation(project(":shared:jackson"))
    implementation(project(":shared:logging"))
    // kafka
    api("org.springframework.kafka:spring-kafka")
    // test
    testImplementation("org.springframework.kafka:spring-kafka-test")
}
