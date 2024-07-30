import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

class AndroidApplicationConfigPlugin : Plugin<Project> {

    companion object {
        private const val MAX_API_LEVEL = 34
        private const val MIN_API_LEVEL = 24
    }

    override fun apply(project: Project) {

        with(project.pluginManager) {
            apply("com.android.application")
            apply("org.jetbrains.kotlin.android")
        }

        project.extensions.configure<BaseAppModuleExtension> {

            compileSdk = MAX_API_LEVEL

            buildFeatures.viewBinding = true
            buildFeatures.buildConfig = true

            defaultConfig {
                minSdk = MIN_API_LEVEL
                targetSdk = MAX_API_LEVEL
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                vectorDrawables.useSupportLibrary = true
            }

            buildTypes {
                getByName("debug") {
                    isMinifyEnabled = false
                    isJniDebuggable = true
                    signingConfig = signingConfigs.getByName("debug")
                }
                getByName("release") {
                    isMinifyEnabled = true
                    signingConfig = signingConfigs.getByName("debug")
                }
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }

            project.tasks.withType(KotlinCompile::class.java).configureEach {
                kotlinOptions {
                    jvmTarget = "11"
                }
            }

        }


    }

}



