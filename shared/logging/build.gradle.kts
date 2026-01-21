plugins {
    `java-library`
}

dependencies {

    // logging facade
    api("org.slf4j:slf4j-api")

    // Spring MVC (HandlerInterceptor 포함)
    api("org.springframework:spring-webmvc")

    // Servlet API (컴파일 시에만 필요)
    compileOnly("jakarta.servlet:jakarta.servlet-api")
}
