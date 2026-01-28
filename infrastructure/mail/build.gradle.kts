plugins {
    `java-library`
}

dependencies {
    // project modules
    implementation(project(":shared:logging"))
    // mail
    api("org.springframework.boot:spring-boot-starter-mail")
}
