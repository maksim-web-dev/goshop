plugins {
    kotlin("jvm") version "2.2.0" apply false
    kotlin("plugin.spring") version "2.2.0" apply false
    kotlin("plugin.jpa") version "2.2.0" apply false
    id("org.springframework.boot") version "3.3.5" apply false
    id("io.spring.dependency-management") version "1.1.6" apply false
}

subprojects {
    group = "com"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}