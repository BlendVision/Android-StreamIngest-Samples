dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("library") { from(files("../gradle/libs.versions.toml")) }
    }
}

rootProject.name = "build-logic"