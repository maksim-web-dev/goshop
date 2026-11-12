plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}
rootProject.name = "api-service"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}
