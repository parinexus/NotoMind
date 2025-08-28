import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply {
            apply("parinexus.android.library")
            apply("parinexus.android.library.compose")
            apply("parinexus.android.hilt")
            apply("org.jetbrains.kotlin.plugin.serialization")
        }

        val libs = extensions.getByType(VersionCatalogsExtension::class.java).named("libs")

        extensions.configure<LibraryExtension> {
            defaultConfig {
                testInstrumentationRunner = "com.parinexus.testing.TestRunner"
            }
            testOptions.animationsDisabled = true
        }

        val enableAndroidTest =
            providers.gradleProperty("enableAndroidTest").map { it.toBoolean() }.orElse(false)

        extensions.configure<LibraryAndroidComponentsExtension> {
            beforeVariants(selector().all()) { builder ->
                builder.androidTest.enable = enableAndroidTest.get()
            }
        }

        dependencies {
            add("implementation", project(":modules:ui"))
            add("implementation", project(":modules:designsystem"))
            add("implementation", project(":modules:data"))
            add("testImplementation", project(":modules:testing"))

            add("implementation", libs.findLibrary("androidx-navigation-compose").get())
            add("implementation", libs.findLibrary("androidx-hilt-navigation-compose").get())
            add("implementation", libs.findLibrary("androidx-lifecycle-runtimeCompose").get())
            add("implementation", libs.findLibrary("androidx-lifecycle-viewModelCompose").get())
            add("implementation", libs.findLibrary("androidx-tracing-ktx").get())
            add("implementation", libs.findLibrary("kotlinx-serialization-json").get())

            add("testImplementation", libs.findLibrary("junit4").get())
            add("testImplementation", libs.findLibrary("kotlinx-coroutines-test").get())
            add("testImplementation", libs.findLibrary("turbine").get())
            add("testImplementation", libs.findLibrary("mockk").get())
            add("testImplementation", libs.findLibrary("robolectric").get())
            add("testImplementation", libs.findLibrary("androidx-navigation-testing").get())

            if (enableAndroidTest.get()) {
                add("androidTestImplementation", project(":modules:testing"))
                add("androidTestImplementation", libs.findLibrary("mockk").get())
                add("androidTestImplementation", libs.findLibrary("androidx-lifecycle-runtimeTesting").get())
            }
        }
    }
}