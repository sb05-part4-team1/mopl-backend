plugins {
    `java-library`
    `java-test-fixtures`
}

dependencies {
    // Test (순수 JUnit, Spring 제외)
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.mockito:mockito-junit-jupiter")

    // Test Fixtures (Fixture Monkey)
    testFixturesImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter:${project.properties["fixtureMonkeyVersion"]}")
}
