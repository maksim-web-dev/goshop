plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}
rootProject.name = "search-service"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}
