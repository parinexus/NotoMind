import com.parinexus.app.BuildType

plugins {
    id("parinexus.android.application")
    id("parinexus.android.application.compose")
    id("parinexus.android.application.flavor")
    id("parinexus.android.hilt")
}

android {
    namespace = "com.parinexus.playnotepad"

    defaultConfig {
        applicationId = "com.parinexus.notomind"
        versionCode = libs.versions.versionCode.get().toIntOrNull()
        versionName = System.getenv("VERSION_NAME") ?: libs.versions.versionName.get()

        testInstrumentationRunner = "com.parinexus.testing.TestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = BuildType.DEBUG.applicationIdSuffix
            versionNameSuffix=BuildType.DEBUG.versionNameSuffix

        }

        getByName("release") {
            isMinifyEnabled = true
            applicationIdSuffix = BuildType.RELEASE.applicationIdSuffix
            versionNameSuffix= BuildType.RELEASE.versionNameSuffix

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(projects.modules.designsystem)
    implementation(projects.modules.data)
    implementation(projects.modules.domain)
    implementation(projects.modules.ui)

    implementation(projects.feature.main)
    implementation(projects.feature.detail)
    implementation(projects.feature.gallery)
    implementation(projects.feature.labelscreen)
    implementation(projects.feature.selectlabelscreen)
    implementation(projects.feature.setting)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3.adaptive)
    implementation(libs.androidx.compose.material3.adaptive.layout)
    implementation(libs.androidx.compose.material3.adaptive.navigation)
    implementation(libs.androidx.compose.material3.windowSizeClass)
    implementation(libs.androidx.compose.runtime.tracing)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtimeCompose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.tracing.ktx)
    implementation(libs.androidx.window.core)
    implementation(libs.kotlinx.coroutines.guava)
    implementation(libs.coil.kt)
    implementation(libs.timber)

    ksp(libs.hilt.compiler)

    kspTest(libs.hilt.compiler)

    testImplementation(projects.modules.testing)
    testImplementation(libs.androidx.compose.ui.test)
    testImplementation(libs.androidx.work.testing)
    testImplementation(libs.hilt.android.testing)

    androidTestImplementation(projects.modules.testing)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.hilt.android.testing)

    googlePlayImplementation(platform(libs.firebase.bom))
    googlePlayImplementation(libs.firebase.analytics)
    googlePlayImplementation(libs.firebase.performance)
    googlePlayImplementation(libs.firebase.crashlytics)

    googlePlayImplementation(libs.firebase.cloud.messaging)
    googlePlayImplementation(libs.firebase.remoteconfig)
    googlePlayImplementation(libs.firebase.message)
    googlePlayImplementation(libs.firebase.auth)

    googlePlayImplementation(libs.play.game)
    googlePlayImplementation(libs.play.update)
    googlePlayImplementation(libs.play.update.kts)
    googlePlayImplementation(libs.play.review)
    googlePlayImplementation(libs.play.review.kts)
}

dependencyGuard {
    configuration("fossReliantReleaseRuntimeClasspath")
    configuration("googlePlayReleaseRuntimeClasspath")
}
