plugins {
    `java-library`
}

dependencies {
    // actuator & prometheus
    api("org.springframework.boot:spring-boot-starter-actuator")
    api("io.micrometer:micrometer-registry-prometheus")
    // distributed tracing
    api("io.micrometer:micrometer-tracing-bridge-brave:${project.properties["micrometerTracingVersion"]}")
    api("io.zipkin.reporter2:zipkin-reporter-brave:${project.properties["zipkinReporterVersion"]}")
}
