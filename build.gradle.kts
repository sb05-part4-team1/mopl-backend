import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    id("org.springframework.boot") version "3.3.3" apply false
    id("io.spring.dependency-management") version "1.1.5"
    id("com.diffplug.spotless") version "6.23.3"
    checkstyle
    jacoco
}

val springBootVersion: String by project
val checkstyleVersion = "10.12.3"

val jacocoExclusions = listOf(
    "**/entity/**/Q*.class",
    "**/*Application.class"
)

val jacocoAggregateExclusions = jacocoExclusions + listOf(
    "**/*Config.class",
    "**/*Config$*.class",
    "**/*Properties.class",
    "**/*Properties$*.class",
    "**/*Event.class",
    "**/*Event$*.class",
)

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
            mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
        }
    }

    dependencies {
        implementation("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")
        testCompileOnly("org.projectlombok:lombok")
        testAnnotationProcessor("org.projectlombok:lombok")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    if (!project.path.startsWith(":core")) {
        dependencies {
            implementation("org.springframework.boot:spring-boot-starter")
            testImplementation("org.springframework.boot:spring-boot-starter-test")
        }
    }

    configureJarTasks()
    configureTestTasks()
    configureJacoco()
    configureSpotless()
    configureCheckstyle()
}

fun Project.configureJarTasks() {
    tasks.withType<Jar> { enabled = true }
    tasks.withType<BootJar> { enabled = false }

    if (parent?.name == "applications") {
        apply(plugin = "org.springframework.boot")
        tasks.withType<Jar> { enabled = false }
        tasks.withType<BootJar> { enabled = true }
    }
}

fun Project.configureTestTasks() {
    tasks.withType<JavaCompile> {
        options.compilerArgs.add("-parameters")
    }

    tasks.test {
        maxParallelForks = 1
        useJUnitPlatform()
        systemProperty("user.timezone", "Asia/Seoul")
        systemProperty("spring.profiles.active", "test")
        jvmArgs("-Xshare:off")
    }
}

fun Project.configureJacoco() {
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
                files(classDirectories.files.map { fileTree(it).exclude(jacocoExclusions) })
            )
        }
    }
}

fun Project.configureSpotless() {
    spotless {
        java {
            eclipse().configFile("$rootDir/config/eclipse/eclipse-java-formatter.xml")
            target("src/**/*.java")
            removeUnusedImports()
            endWithNewline()
        }
    }
}

fun Project.configureCheckstyle() {
    checkstyle {
        toolVersion = checkstyleVersion
        configFile = file("$rootDir/config/checkstyle/google_checks.xml")
    }

    tasks.withType<Checkstyle>().configureEach {
        exclude("**/*ApiSpec.java")
    }
}

listOf("applications", "core", "infrastructure", "shared").forEach { name ->
    project(name) { tasks.configureEach { enabled = false } }
}

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
        files(subprojects.flatMap { it.the<SourceSetContainer>()["main"].allSource.srcDirs })
    )

    classDirectories.setFrom(
        files(subprojects.flatMap { subproject ->
            subproject.the<SourceSetContainer>()["main"].output.classesDirs.map {
                fileTree(it).exclude(jacocoAggregateExclusions)
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
