plugins {
    `java-library`
}

dependencies {
    api(project(":core:domain"))
    api(project(":infrastructure:storage"))
    api(project(":shared:jackson"))

    testImplementation(testFixtures(project(":core:domain")))
}
