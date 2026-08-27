pluginManagement {
    repositories {
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
        // Huawei Map Kit / HMS Core artifacts are only published on Huawei's own repo.
        maven { url = uri("https://developer.huawei.com/repo/") }
    }
}

rootProject.name = "android-live-delivery-tracking-sdk"
include(":app")
include(":feature:tracking")
include(":core:location-abstraction")
include(":core:network")
include(":core:database")
