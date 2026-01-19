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

    // Lombok (IDE 인식 보강용 – 루트에 있어도 명시 추천)
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // (선택) logback을 여기서 통제하고 싶다면
    // implementation("ch.qos.logback:logback-classic")
}
