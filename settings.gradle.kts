pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}
rootProject.name = "goshop"

include(
    "product-service",
    "localization-service",
    "search-service",
    "store-service",
    "api-service",
    "pricing-service",
    "compatibility-service",
    "recommendations-service"
)

