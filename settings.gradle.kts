rootProject.name = "MASCostos"

val currentJava = JavaVersion.current()
if (!currentJava.isCompatibleWith(JavaVersion.VERSION_17) || currentJava > JavaVersion.VERSION_21) {
    throw GradleException(
        "MASCostos requiere Java 17 o Java 21. JAVA_HOME actual: ${System.getProperty("java.version")}" +
            ". Configure JAVA_HOME a JDK 17/21 e intente nuevamente."
    )
}

include("MAS-Core")
include("MAS-App")
include("MAS-Data-Sqlite")
include("MAS-UI-Desktop")
