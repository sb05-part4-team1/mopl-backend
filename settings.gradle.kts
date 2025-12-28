rootProject.name = "mopl-backend"

include(
    ":applications:api",
    // ":applications:chat",
    // ":applications:batch",
    // ":applications:streamer",
    ":cores:domain",
    ":modules:jpa",
    ":modules:security",
    // ":modules:redis",
    // ":modules:kafka",
    // ":modules:external",
    ":supports:jackson",
    // ":supports:logging",
    // ":supports:monitoring",
)
