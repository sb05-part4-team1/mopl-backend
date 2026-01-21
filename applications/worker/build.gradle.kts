dependencies {
    // project modules
    implementation(project(":core:domain"))
    implementation(project(":infrastructure:jpa"))
    implementation(project(":infrastructure:redis"))
    implementation(project(":infrastructure:kafka"))
    implementation(project(":shared:jackson"))
    // actuator
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    // shedlock
    implementation("net.javacrumbs.shedlock:shedlock-spring:6.2.0")
    implementation("net.javacrumbs.shedlock:shedlock-provider-redis-spring:6.2.0")
    // test
    testImplementation(testFixtures(project(":core:domain")))
}
