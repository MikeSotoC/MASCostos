plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(project(":mas-api"))
    implementation(project(":mas-core"))

    implementation("io.ktor:ktor-server-core-jvm:2.3.12")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.12")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.12")
    implementation("io.ktor:ktor-serialization-gson-jvm:2.3.12")
    implementation("org.xerial:sqlite-jdbc:3.46.1.3")
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.uchi.mascostos.backend.ServerMainKt")
}
