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
    }
    versionCatalogs {
        create("libraries") { from(files("gradle/libraries.versions.toml")) }
    }
}

rootProject.name = "Android-Streamingest-SDK"
include(":streamingest-sample")
