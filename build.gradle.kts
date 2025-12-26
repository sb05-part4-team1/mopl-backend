import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    id("org.springframework.boot") version "3.3.3" apply false
    id("io.spring.dependency-management") version "1.1.5"
    id("com.diffplug.spotless") version "6.23.3"
    id("checkstyle")
    id("jacoco")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

allprojects {
    group = "com.mopl"
    version = "0.0.1"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "checkstyle")
    apply(plugin = "jacoco")

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.3")
        }
    }

    dependencies {
        // Web
        runtimeOnly("org.springframework.boot:spring-boot-starter-validation")
        // Spring
        implementation("org.springframework.boot:spring-boot-starter")
        // Serialize
        implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
        // Lombok
        implementation("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")
        testCompileOnly("org.projectlombok:lombok")
        testAnnotationProcessor("org.projectlombok:lombok")
        // Test
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType(Jar::class) { enabled = true }
    tasks.withType(BootJar::class) { enabled = false }

    configure(allprojects.filter { it.parent?.name.equals("applications") }) {
        tasks.withType(Jar::class) { enabled = false }
        tasks.withType(BootJar::class) { enabled = true }
    }

    tasks.test {
        maxParallelForks = 1
        useJUnitPlatform()
        systemProperty("user.timezone", "Asia/Seoul")
        systemProperty("spring.profiles.active", "test")
        jvmArgs("-Xshare:off")
        finalizedBy(tasks.jacocoTestReport)
    }

    tasks.withType<JacocoReport> {
        mustRunAfter("test")
        executionData(fileTree(layout.buildDirectory.asFile).include("jacoco/*.exec"))
        reports {
            xml.required = true
            csv.required = false
            html.required = false
        }
        afterEvaluate {
            classDirectories.setFrom(
                files(
                    classDirectories.files.map {
                        fileTree(it).exclude("**/entity/**/Q*.class")
                    },
                ),
            )
        }
    }

    spotless {
        java {
            eclipse()
                .configFile("${rootDir}/config/eclipse/eclipse-java-formatter.xml")
            target("src/**/*.java")
            removeUnusedImports()
            endWithNewline()
        }
    }

    checkstyle {
        toolVersion = "10.12.3"
        configFile = file("${rootDir}/config/checkstyle/google_checks.xml")
    }
}

project("applications") { tasks.configureEach { enabled = false } }
project("cores") { tasks.configureEach { enabled = false } }
project("modules") { tasks.configureEach { enabled = false } }
project("supports") { tasks.configureEach { enabled = false } }

tasks.named<JacocoReport>("jacocoTestReport") {
    description = "Generates an aggregate JaCoCo report from all subprojects"

    dependsOn(subprojects.mapNotNull { it.tasks.findByName("jacocoTestReport") })

    executionData.setFrom(
        files(subprojects.flatMap { subproject ->
            subproject.layout.buildDirectory.asFile.get()
                .resolve("jacoco")
                .listFiles()
                ?.filter { it.extension == "exec" }
                ?: emptyList()
        })
    )

    sourceDirectories.setFrom(
        files(subprojects.flatMap { subproject ->
            subproject.the<SourceSetContainer>()["main"].allSource.srcDirs
        })
    )

    classDirectories.setFrom(
        files(subprojects.flatMap { subproject ->
            subproject.the<SourceSetContainer>()["main"].output.classesDirs.map {
                fileTree(it).exclude("**/entity/**/Q*.class")
            }
        })
    )

    reports {
        xml.required = true
        html.required = true
        html.outputLocation = layout.buildDirectory.dir("reports/jacoco/aggregate/html")
        xml.outputLocation = layout.buildDirectory.file("reports/jacoco/aggregate/jacocoTestReport.xml")
    }
}
