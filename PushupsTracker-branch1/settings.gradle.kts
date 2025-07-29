// File: settings.gradle.kts (Project Root)
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // ✅ ADD THIS LINE: Tells Gradle to also look in JitPack
        maven { url = uri("https://jitpack.io") }
    }
}
rootProject.name = "PushupsTracker"
include(":app")