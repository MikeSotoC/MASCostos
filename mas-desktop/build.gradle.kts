plugins {
    kotlin("jvm")
    application
    id("org.openjfx.javafxplugin")
}

dependencies {
    implementation(project(":mas-api"))
    implementation(project(":mas-core"))
    implementation(project(":mas-shared-ui"))
    implementation("io.github.mkpaz:atlantafx-base:2.0.1")
}

javafx {
    version = "21"
    modules("javafx.controls")
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.uchi.mascostos.desktop.DesktopMainKt")
}
