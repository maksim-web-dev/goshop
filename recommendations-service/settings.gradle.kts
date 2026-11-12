plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}
rootProject.name = "recommendations-service"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}