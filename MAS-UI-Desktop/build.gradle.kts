plugins {
    kotlin("jvm")
    id("org.openjfx.javafxplugin")
    application
}

group = "com.uchi.mascostos"
version = "1.0.0"

kotlin {
    jvmToolchain(21)
}

javafx {
    version = "21.0.2"
    modules = listOf(
        "javafx.controls",
        "javafx.fxml"
    )
}

dependencies {
    implementation(project(":MAS-Core"))
    implementation(project(":MAS-App"))
    implementation(project(":MAS-Data-Sqlite"))

    implementation("io.github.mkpaz:atlantafx-base:2.0.1")
}

application {
    mainClass.set("com.uchi.mascostos.ui.desktop.DesktopAppKt")
}