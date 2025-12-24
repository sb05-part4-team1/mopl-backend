rootProject.name = "mopl-backend"

include(
    "applications:mopl-api",
    // ":applications:mopl-chat",
    // ":applications:mopl-batch",
    // ":applications:mopl-streamer",
    "modules:jpa",
    // ":modules:redis",
    // ":modules:kafka",
    // ":modules:external",
     ":supports:jackson",
    // ":supports:logging",
    // ":supports:monitoring",
)
