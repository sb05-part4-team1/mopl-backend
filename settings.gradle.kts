rootProject.name = "mopl-backend"

include(
    ":applications:api",
    ":applications:sse",
    ":applications:websocket",
    ":applications:consumer",
    ":applications:batch",
    ":core:domain",
    ":infrastructure:jpa",
    ":infrastructure:security",
    ":infrastructure:storage",
    ":infrastructure:cache",
    ":infrastructure:redis",
    ":infrastructure:kafka",
    ":infrastructure:mail",
    ":infrastructure:openapi",
    ":shared:jackson",
    // ":shared:logging",
    ":shared:monitoring"
)
