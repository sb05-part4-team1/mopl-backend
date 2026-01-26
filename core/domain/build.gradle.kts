plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    // spring cache
    implementation("org.springframework:spring-context")
    // jackson for event serialization
    implementation("com.fasterxml.jackson.core:jackson-databind")
    // Test
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
    // Fixturemonkey
    testFixturesApi("com.navercorp.fixturemonkey:fixture-monkey-starter:${project.properties["fixtureMonkeyVersion"]}")
}
