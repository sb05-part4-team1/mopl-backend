rootProject.name = "mopl-backend"

include(
    ":applications:api",
    ":applications:sse",
    ":applications:websocket",
    // ":applications:chat",
    // ":applications:batch",
    // ":applications:streamer",
    ":core:domain",
    ":infrastructure:jpa",
    ":infrastructure:security",
    ":infrastructure:storage",
    ":infrastructure:cache",
    ":infrastructure:redis",
    // ":infrastructure:kafka",
    // ":infrastructure:external",
    ":shared:jackson",
    // ":shared:logging",
    // ":shared:monitoring",
)
