plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":mas-api"))
    implementation("org.xerial:sqlite-jdbc:3.46.1.3")
    implementation("org.apache.pdfbox:pdfbox:2.0.31")
}

kotlin {
    jvmToolchain(21)
}
