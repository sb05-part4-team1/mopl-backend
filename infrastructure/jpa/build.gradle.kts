plugins {
    `java-library`
}

dependencies {
    // project module
    implementation(project(":core:domain"))
    // jpa
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    // querydsl
    api("com.querydsl:querydsl-jpa::jakarta")
    annotationProcessor("com.querydsl:querydsl-apt::jakarta")
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
    annotationProcessor("jakarta.annotation:jakarta.annotation-api")
    // jdbc-mysql
    runtimeOnly("com.mysql:mysql-connector-j")
    // jdbc-h2
    runtimeOnly("com.h2database:h2")
    // uuid v7
    implementation("com.fasterxml.uuid:java-uuid-generator:${project.properties["javaUuidGeneratorVersion"]}")
}
