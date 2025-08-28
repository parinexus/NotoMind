plugins {
    id("parinexus.android.library")
    id("parinexus.android.library.compose")

    id("parinexus.android.hilt")
}

android {
    namespace = "com.parinexus.analytics"
}

dependencies {
    implementation(libs.androidx.compose.runtime)
    googlePlayImplementation(platform(libs.firebase.bom))
    googlePlayImplementation(libs.firebase.analytics)
}
