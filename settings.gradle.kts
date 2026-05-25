rootProject.name = "maestro"
include(":core", ":shared", ":androidApp", ":webApp", ":server")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
