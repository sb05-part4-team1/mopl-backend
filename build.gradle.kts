plugins {
    id("java")
    id("org.springframework.boot") version "3.3.3" apply false
    id("io.spring.dependency-management") version "1.1.5"
    id("com.diffplug.spotless") version "6.23.3"
    id("checkstyle")
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

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
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

    // ✅ Kotlin DSL에서는 여기서 설정
    tasks.withType<Checkstyle>().configureEach {
        isIgnoreFailures = false
    }

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.3.3")
        }
    }

    dependencies {
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")

        testCompileOnly("org.projectlombok:lombok")
        testAnnotationProcessor("org.projectlombok:lombok")

        implementation("org.springframework.boot:spring-boot-starter-validation")

        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }
}