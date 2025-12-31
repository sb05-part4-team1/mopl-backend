rootProject.name = "mopl-backend"

include(
    ":applications:api",
    // ":applications:chat",
    // ":applications:batch",
    // ":applications:streamer",
    ":core:domain",
    ":infrastructure:jpa",
    ":infrastructure:security",
    // ":infrastructure:redis",
    // ":infrastructure:kafka",
    // ":infrastructure:external",
    ":shared:jackson",
    // ":shared:logging",
    // ":shared:monitoring",
)
