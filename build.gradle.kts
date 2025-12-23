plugins {
    id("java")
    id("org.springframework.boot") version "3.3.3" apply false
    id("io.spring.dependency-management") version "1.1.5"
    id("com.diffplug.spotless") version "6.23.3"
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

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    spotless {
        java {
            googleJavaFormat("1.17.0").aosp()
            target("src/**/*.java")
        }
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