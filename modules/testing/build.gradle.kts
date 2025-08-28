plugins {
    id("parinexus.android.library")
    id("parinexus.android.library.compose")
    id("parinexus.android.hilt")
}

android {
    namespace = "com.parinexus.testing"
}
dependencies {
    api(kotlin("test"))
    api(libs.androidx.compose.ui.test)
    api(projects.modules.analytics)
    api(projects.modules.data)
    api(projects.modules.model)

    debugApi(libs.androidx.compose.ui.testManifest)

    api(libs.androidx.test.core)
    api(libs.androidx.compose.ui.test)

    api(libs.turbine)
    api(libs.androidx.test.rules)
    api(libs.hilt.android.testing)
    api(libs.kotlinx.coroutines.test)
    api(libs.kotlinx.datetime)
    implementation(projects.modules.common)
    implementation(projects.modules.designsystem)
}