plugins {
    `java-library`
}

dependencies {
    // logging
    api("ch.qos.logback:logback-classic")
    api("net.logstash.logback:logstash-logback-encoder:${project.properties["logstashLogbackEncoderVersion"]}")
    // slack appender
    api("com.github.maricn:logback-slack-appender:${project.properties["slackAppenderVersion"]}")
    // spring (optional - for MdcLoggingFilter)
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-autoconfigure")
    compileOnly("org.springframework.security:spring-security-core")
}
