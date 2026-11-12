plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}
rootProject.name = "product-service"

pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}