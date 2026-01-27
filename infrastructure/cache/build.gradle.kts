plugins {
    `java-library`
}

dependencies {
    // project modules
    implementation(project(":core:domain"))
    implementation(project(":infrastructure:redis"))
    implementation(project(":shared:logging"))
    // caffeine
    implementation("com.github.ben-manes.caffeine:caffeine")
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    // validation
    implementation("org.springframework.boot:spring-boot-starter-validation")
}
