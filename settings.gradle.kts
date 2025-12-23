rootProject.name = "mopl-backend"

include(
    "app:api",
    "app:batch",
    "modules:jpa",
    "modules:redis",
    "modules:kafka",
    "modules:external",
    "supports:monitoring",
)