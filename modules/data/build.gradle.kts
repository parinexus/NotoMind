plugins {
    id("parinexus.android.library")
    id("parinexus.android.hilt")
    id("kotlinx-serialization")
}

android {
    namespace = "com.parinexus.data"
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(projects.modules.database)
    implementation(projects.modules.datastore)
    api(projects.modules.domain)
    api(projects.modules.common)
    api(projects.modules.model)
    implementation(projects.modules.analytics)

    implementation(libs.kotlinx.datetime)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(projects.modules.testing)

    testImplementation(libs.junit4)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlin.test.junit5)

    debugImplementation(libs.androidx.compose.ui.testManifest)
}
