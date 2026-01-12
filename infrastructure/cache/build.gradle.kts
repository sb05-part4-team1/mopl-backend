plugins {
    `java-library`
}

dependencies {
    // project modules
    implementation(project(":core:domain"))
    implementation(project(":infrastructure:redis"))
    // caffeine
    implementation("com.github.ben-manes.caffeine:caffeine")
    // validation
    implementation("org.springframework.boot:spring-boot-starter-validation")
}
