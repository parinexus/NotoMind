plugins {
    id("parinexus.android.feature")
}

android {
    namespace = "com.parinexus.detail"
}

dependencies {
    implementation(projects.modules.data)
    testImplementation(libs.hilt.android.testing)
    testImplementation(projects.modules.testing)
    implementation(libs.kotlinx.datetime)


    androidTestImplementation(projects.modules.testing)
}
