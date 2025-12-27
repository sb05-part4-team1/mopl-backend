plugins {
    `java-library`
}

dependencies {
    // domain
    implementation(project(":core:domain"))

    // spring security
    api("org.springframework.boot:spring-boot-starter-security")
    api("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // servlet
    compileOnly("jakarta.servlet:jakarta.servlet-api")
}
