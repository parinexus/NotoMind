pluginManagement {
    repositories {
        includeBuild("build-logic")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://androidx.dev/storage/compose-compiler/repository/")
    }
}
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "NotoMind"
include(":app")

include(":modules:data")
include(":modules:datastore")
include(":modules:model")
include(":modules:common")
include(":modules:testing")
include(":modules:database")
include(":modules:analytics")
include(":modules:designsystem")
include(":modules:domain")
include(":modules:ui")
include(":feature:labelscreen")
include(":feature:selectlabelscreen")
include(":feature:gallery")
include(":feature:main")
include(":feature:detail")
include(":feature:setting")



