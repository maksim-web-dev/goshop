plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}
rootProject.name = "store-service"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}