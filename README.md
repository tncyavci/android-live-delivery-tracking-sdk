# Android Live Delivery & Courier Tracking Engine 🚚

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-blue.svg)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-SDK%2034-green.svg)](https://developer.android.com)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.6.0-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![Architecture](https://img.shields.io/badge/Architecture-Multi--Module%20Clean%20MVI-orange.svg)](https://developer.android.com/topic/architecture)
[![License](https://img.shields.io/badge/License-MIT-purple.svg)](LICENSE)

A production-grade, highly performant **Android Live Delivery & Courier Tracking Engine** designed for high-concurrency express delivery and e-commerce platforms (such as fast grocery and food delivery services).

This SDK provides a **Map Provider Abstraction Layer** (seamlessly unifying Google Maps, Huawei Location Kit, and Yandex MapKit under a single Strategy Pattern), smooth **60 FPS marker interpolation** for real-time WebSocket telemetry, and an **offline-first local persistence layer** powered by Room and WorkManager for zero data loss during cellular signal drops.

---

## 🌟 Key Architecture & Features

- **🗺️ Map Provider Abstraction Layer (Strategy + Factory):** Unified `MapAdapter` interface supporting runtime switching between **GMS (Google Maps / Places)**, **HMS (Huawei Location Kit)**, and **Yandex MapKit**.
- **⚡ Smooth Courier Marker Animation (Lerp Algorithm):** 60 FPS linear/spherical marker position and bearing interpolation, eliminating jarring marker jumps on 2-3s WebSocket telemetry intervals.
- **🛡️ Offline-First Resilience (Room + WorkManager):** Tunnel and cellular drop handling — buffers unsynced courier telemetry into local Room DB and triggers `CoroutineWorker` background sync upon network recovery.
- **🏗️ Multi-Module Clean MVI Architecture:** Decoupled Gradle modules (`:app`, `:feature:tracking`, `:core:location-abstraction`, `:core:network`, `:core:database`) for optimal build performance and developer velocity.
- **🔔 Push Notification Lifecycle (FCM Integration):** Handlers for Firebase Cloud Messaging events triggering "Courier nearby" and "Order status updated" bottom-sheet UI states.

---

## 📐 Multi-Module Architecture Graph

```
                                ┌────────────────────────┐
                                │       :app Module      │
                                └───────────┬────────────┘
                                            │
                                ┌───────────▼────────────┐
                                │   :feature:tracking    │
                                │  (Compose UI, State)   │
                                └───────────┬────────────┘
                                            │
               ┌────────────────────────────┼────────────────────────────┐
               │                            │                            │
   ┌───────────▼────────────┐   ┌───────────▼────────────┐   ┌───────────▼────────────┐
   │ :core:location-abstract│   │     :core:network      │   │    :core:database      │
   │ (GMS/HMS/Yandex Interf)│   │  (WebSocket, Retrofit) │   │ (Room, Offline Cache)  │
   └────────────────────────┘   └────────────────────────┘   └────────────────────────┘
```

---

## 🛠️ Quick Start & Setup Instructions

### Prerequisites
- **Android Studio:** Jellyfish (2023.3.1) or higher
- **JDK:** OpenJDK 17 or 21
- **Gradle:** 8.5+
- **Min SDK:** 26 (Android 8.0) — set by Yandex MapKit, the strictest of the three map providers
- **Target SDK:** 34 (Android 14)

### 1. Clone the Repository
```bash
git clone https://github.com/tncyavci/android-live-delivery-tracking-sdk.git
cd android-live-delivery-tracking-sdk
```

### 2. Configure Map API Keys
Add your Google Maps and Yandex MapKit API keys to `local.properties` (never commit keys to version control):

```properties
MAPS_API_KEY=AIzaSyYourGoogleMapsApiKeyHere
YANDEX_MAPKIT_API_KEY=your-yandex-mapkit-api-key-here
```

### 3. Build & Run
```bash
# Run static analysis and lint checks
./gradlew ktlintCheck detekt

# Run unit tests
./gradlew test

# Build debug APK
./gradlew assembleDebug
```

---

## 💻 Core Code Example: Map Provider Abstraction

```kotlin
// core/location-abstraction/MapAdapter.kt
interface MapAdapter {
    fun initializeMap(context: Context, container: Any)
    fun updateCourierLocation(location: CourierLocation, animate: Boolean)
    fun setRoutePolyline(points: List<LatLngPoint>)
    fun setMapBounds(courierLocation: CourierLocation, userLocation: CourierLocation)
    fun clearMap()
}

// Automatic Provider Injection based on device capability
@Provides
@Singleton
fun provideMapAdapter(
    @ApplicationContext context: Context,
    gmsAdapter: Provider<GmsMapAdapter>,
    hmsAdapter: Provider<HmsMapAdapter>,
    yandexAdapter: Provider<YandexMapAdapter>
): MapAdapter {
    val isGmsAvailable = GoogleApiAvailability.getInstance()
        .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
    return when {
        isGmsAvailable -> gmsAdapter.get()
        isHuaweiDevice() -> hmsAdapter.get()
        else -> yandexAdapter.get()
    }
}
```

---

## 🧪 Testing & Verification

- **Unit Tests:** Run `./gradlew :feature:tracking:test` for ViewModel StateFlow emissions and Lerp interpolation tests.
- **Location Simulator:** Built-in `MockLocationEngine` generates simulated GPS courier movements for testing without a physical device.

---

## 📄 Documentation

For full architectural specifications, data schemas, and design decisions, see [TECHNICAL_SPECIFICATION.md](TECHNICAL_SPECIFICATION.md).

## 📜 License
MIT License. Created by [Tuncay Avcı](https://github.com/tncyavci).
