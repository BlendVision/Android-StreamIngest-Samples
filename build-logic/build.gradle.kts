plugins {
    `kotlin-dsl`
}
repositories {
    // The org.jetbrains.kotlin.jvm plugin requires a repository
    // where to download the Kotlin compiler dependencies from.
    mavenCentral()
    google()
    gradlePluginPortal()
}

dependencies {
    compileOnly(library.android.gradlePlugin)
    compileOnly(library.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplicationConfigPlugin") {
            id = "com.blendvision.stream.ingest.sample.plugin.android.application.config"
            implementationClass = "AndroidApplicationConfigPlugin"
        }
    }
}
