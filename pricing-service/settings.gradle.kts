plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}
rootProject.name = "pricing-service"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}