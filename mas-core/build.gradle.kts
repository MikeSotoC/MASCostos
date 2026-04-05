plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":mas-api"))
    implementation("org.xerial:sqlite-jdbc:3.46.1.3")
}

kotlin {
    jvmToolchain(21)
}
