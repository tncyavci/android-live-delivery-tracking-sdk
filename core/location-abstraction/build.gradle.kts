plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.tuncayavci.tracking.location"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)

    // Exposed as `api`: consumers building the AndroidView bridge (see
    // feature:tracking's CourierMapView) need the concrete vendor MapView types, not just
    // the MapAdapter abstraction, since Compose interop requires a real android.view.View.
    api(libs.play.services.maps)
    api(libs.huawei.maps)
    api(libs.yandex.mapkit)
    implementation(libs.play.services.location)
    implementation(libs.huawei.base)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.robolectric)
}
