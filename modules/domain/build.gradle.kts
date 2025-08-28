plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
    }
}

dependencies {
    api(projects.modules.model)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    api(libs.javax.inject)
    implementation(projects.modules.common)

}