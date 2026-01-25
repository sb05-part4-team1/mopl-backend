rootProject.name = "mopl-backend"

include(
    ":applications:api",
    ":applications:batch",
    ":applications:sse",
    ":applications:websocket",
    ":applications:worker",
    ":core:domain",
    ":infrastructure:cache",
    ":infrastructure:jpa",
    ":infrastructure:kafka",
    ":infrastructure:mail",
    ":infrastructure:openapi",
    ":infrastructure:redis",
    ":infrastructure:search",
    ":infrastructure:security",
    ":infrastructure:storage",
    ":shared:dto",
    ":shared:jackson",
    ":shared:logging",
    ":shared:monitoring"
)
