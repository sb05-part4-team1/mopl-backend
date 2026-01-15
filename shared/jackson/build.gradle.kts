plugins {
    `java-library`
}

dependencies {
    // spring
    api("org.springframework:spring-web")
    // jackson
    api("com.fasterxml.jackson.core:jackson-databind")
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
}
