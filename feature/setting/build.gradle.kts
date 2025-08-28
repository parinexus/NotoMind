plugins {
    id("parinexus.android.feature")
}

android {
    namespace = "com.parinexus.setting"
}

dependencies {
    implementation(libs.kotlinx.datetime)
    implementation(libs.lottie.compose)
}