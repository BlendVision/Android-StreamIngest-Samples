plugins {
    id("com.blendvision.stream.ingest.sample.plugin.android.application.config")
}

android {
    namespace = "com.blendvision.stream.ingest.sample"
}

dependencies {

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar", "*.jar"))))
    implementation(library.streamingest)

    implementation(library.navigation.fragment.ktx)
    implementation(library.navigation.ui.ktx)
    implementation(library.lifecycle.runtime.ktx)

    implementation(library.bundles.common)

}
