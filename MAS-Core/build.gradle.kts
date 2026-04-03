plugins {
    kotlin("jvm")
}

group = "com.uchi.mascostos"
version = "1.0.0"

val toolchainVersion = (findProperty("mascostos.java.toolchain") as String?)?.toIntOrNull() ?: 17

kotlin {
    jvmToolchain(toolchainVersion)
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}