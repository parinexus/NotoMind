plugins {
    `kotlin-dsl`
}

group = "com.parinexus.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.android.tools.common)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.firebase.crashlytics.gradlePlugin)
    compileOnly(libs.firebase.performance.gradlePlugin)
    implementation(libs.truth)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
    compileOnly(libs.kotlin.powerAssert)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.kover.gradlePlugin)


}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = true
    }
}

gradlePlugin {
    plugins {

        register("androidApplicationCompose") {
            id = "parinexus.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidApplicationFlavor") {
            id = "parinexus.android.application.flavor"
            implementationClass = "AndroidApplicationFlavorsConventionPlugin"
        }
        register("androidApplicationFirebase") {
            id = "parinexus.android.application.firebase"
            implementationClass = "AndroidApplicationFirebaseConventionPlugin"
        }

        register("androidApplication") {
            id = "parinexus.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
       

        register("androidLibraryCompose") {
            id = "parinexus.android.library.compose"
            implementationClass = "AndroidLibraryComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = "parinexus.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "parinexus.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }

        register("androidTest") {
            id = "parinexus.android.test"
            implementationClass = "AndroidTestConventionPlugin"
        }
        register("androidHilt") {
            id = "parinexus.android.hilt"
            implementationClass = "AndroidHiltConventionPlugin"
        }


        register("androidLint") {
            id = "parinexus.android.lint"
            implementationClass = "AndroidLintConventionPlugin"
        }

        register("jvmLibrary") {
            id = "parinexus.jvm.library"
            implementationClass = "JvmLibraryConventionPlugin"
        }

        register("androidRoom") {
            id = "parinexus.android.room"
            implementationClass = "AndroidRoomConventionPlugin"
        }
    }
}
