plugins {
    id("parinexus.android.feature")
}

android {
    namespace = "com.parinexus.gallery"
}
dependencies {
    implementation(libs.telephoto.zoomable.image)
    googlePlayImplementation(libs.play.services.mlkit.text.recognition)
}
