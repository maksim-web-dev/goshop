(plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}
rootProject.name = "localization-service"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}