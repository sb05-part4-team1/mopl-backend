plugins {
    `java-library`
}

dependencies {
    // project modules
    implementation(project(":core:domain"))
    // spring security
    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    // validation
    implementation("org.springframework.boot:spring-boot-starter-validation")
    // servlet
    compileOnly("jakarta.servlet:jakarta.servlet-api")
}
