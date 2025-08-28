

import com.android.build.gradle.LibraryExtension
import com.parinexus.app.configureGradleManagedDevices
import com.parinexus.app.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("parinexus.android.library")
                apply("parinexus.android.library.compose")
                apply("parinexus.android.hilt")
                apply("org.jetbrains.kotlin.plugin.serialization")

            }
            extensions.configure<LibraryExtension> {
                defaultConfig {
                    testInstrumentationRunner =
                        "com.parinexus.testing.TestRunner"
                }
                testOptions.animationsDisabled = true
                configureGradleManagedDevices(this)
            }

            dependencies {
                add("implementation", project(":modules:ui"))
                add("implementation", project(":modules:designsystem"))
                add("implementation", project(":modules:data"))
                add("testImplementation", project(":modules:testing"))
                add("androidTestImplementation", project(":modules:testing"))


                add("implementation", libs.findLibrary("androidx.navigation.compose").get())
                add("implementation", libs.findLibrary("androidx.hilt.navigation.compose").get())
                add("implementation", libs.findLibrary("androidx.lifecycle.runtimeCompose").get())
                add("implementation", libs.findLibrary("androidx.lifecycle.viewModelCompose").get())
                add("implementation", libs.findLibrary("androidx.tracing.ktx").get())

                add("androidTestImplementation", libs.findLibrary("androidx.lifecycle.runtimeTesting").get())

                add("implementation",libs.findLibrary("kotlinx.serialization.json").get())

            }
        }
    }
}
