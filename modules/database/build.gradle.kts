plugins {
    id("parinexus.android.library")

    id("parinexus.android.hilt")
    id("parinexus.android.room")
}

android {
    namespace = "com.parinexus.database"


}

android {
    defaultConfig {
        testInstrumentationRunner = "com.parinexus.testing.TestRunner"

    }
    namespace = "com.parinexus.database"


}

dependencies {
    implementation(project(":modules:model"))
    implementation(libs.kotlinx.datetime)

    testImplementation(libs.junit4)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}