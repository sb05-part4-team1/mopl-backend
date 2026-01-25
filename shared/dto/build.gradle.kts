plugins {
    id("java-library")
}

dependencies {
    api(project(":core:domain"))
    api(project(":infrastructure:storage"))

    testImplementation(testFixtures(project(":core:domain")))
}
