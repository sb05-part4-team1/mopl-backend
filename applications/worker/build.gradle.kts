dependencies {
    // project modules
    implementation(project(":core:domain"))
    implementation(project(":infrastructure:jpa"))
    implementation(project(":infrastructure:redis"))
    implementation(project(":infrastructure:kafka"))
    implementation(project(":shared:jackson"))
    implementation(project(":shared:logging"))
    // shedlock
    implementation("net.javacrumbs.shedlock:shedlock-spring:${project.properties["shedlockVersion"]}")
    implementation("net.javacrumbs.shedlock:shedlock-provider-redis-spring:${project.properties["shedlockVersion"]}")
    // test
    testImplementation(testFixtures(project(":core:domain")))
}
