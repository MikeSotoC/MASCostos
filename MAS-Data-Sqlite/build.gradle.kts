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
    implementation(project(":MAS-Core"))
    implementation("org.xerial:sqlite-jdbc:3.46.1.3")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}