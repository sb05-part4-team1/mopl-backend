plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    // Spring transaction
    implementation("org.springframework:spring-tx")
    // Spring cache
    implementation("org.springframework:spring-context")
    // Test
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
    // Fixturemonkey
    testFixturesApi("com.navercorp.fixturemonkey:fixture-monkey-starter:${project.properties["fixtureMonkeyVersion"]}")
}
