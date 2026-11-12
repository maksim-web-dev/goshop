plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}
rootProject.name = "compatibility-service"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}