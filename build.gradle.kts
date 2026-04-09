plugins {
    base
    kotlin("jvm") version "2.0.21" apply false
    id("org.openjfx.javafxplugin") version "0.1.0" apply false
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
}

allprojects {
    group = "com.uchi.mascostos"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        google()
    }
}
