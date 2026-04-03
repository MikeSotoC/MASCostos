plugins {
    kotlin("jvm")
}

group = "com.uchi.mascostos"
version = "1.0.0"

kotlin {
    jvmToolchain(17)
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}