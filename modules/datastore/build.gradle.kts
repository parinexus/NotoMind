import com.google.protobuf.gradle.GenerateProtoTask

plugins {
    id("parinexus.android.library")
    id("parinexus.android.hilt")
    alias(libs.plugins.protobuf)

}

android {
    defaultConfig {
        consumerProguardFiles("consumer-proguard-rules.pro")
    }
    namespace = "com.parinexus.datastore"
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }

}
// Setup protobuf configuration, generating lite Java and Kotlin classes
protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                register("java") {
                    option("lite")
                }
                register("kotlin") {
                    option("lite")
                }
            }
        }
    }
}


//androidComponents.beforeVariants {
//    android.sourceSets.register(it.name) {
//        val buildDir = layout.buildDirectory.get().asFile
//        java.srcDir(buildDir.resolve("generated/source/proto/${it.name}/java"))
//        kotlin.srcDir(buildDir.resolve("generated/source/proto/${it.name}/kotlin"))
//    }
//}
androidComponents {
    onVariants(selector().all()) { variant ->
        afterEvaluate {
            val protoTask =
                project.tasks.getByName("generate" + variant.name.replaceFirstChar { it.uppercaseChar() } + "Proto") as GenerateProtoTask

            project.tasks.getByName("ksp" + variant.name.replaceFirstChar { it.uppercaseChar() } + "Kotlin") {
                dependsOn(protoTask)
                (this as org.jetbrains.kotlin.gradle.tasks.AbstractKotlinCompileTool<*>).setSource(
                    protoTask.outputBaseDir
                )
            }
        }
    }
}

dependencies {
    api(libs.androidx.dataStore.core)
    api(
        project(":modules:model")
    )
    api(
        project(":modules:common")
    )
    api(libs.protobuf.kotlin.lite)

    testImplementation(libs.kotlinx.coroutines.test)
    implementation(libs.hilt.android.testing)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.datastore.core)
}
