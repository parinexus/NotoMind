plugins {
    id("parinexus.android.feature")
}

android {
    namespace = "com.parinexus.main"
}

dependencies {
    implementation(libs.kotlinx.datetime)
    implementation(libs.lottie.compose)
}