plugins {
    kotlin("jvm") version "2.0.21" apply false
    id("org.openjfx.javafxplugin") version "0.1.0" apply false
}

group = "com.uchi.mascostos"
version = "1.0.0"

subprojects {
    repositories {
        mavenCentral()
    }
}