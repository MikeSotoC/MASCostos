plugins {
    kotlin("jvm")
}

group = "com.uchi.mascostos"
version = "1.0.0"

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":MAS-Core"))
    implementation("org.xerial:sqlite-jdbc:3.46.1.3")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}