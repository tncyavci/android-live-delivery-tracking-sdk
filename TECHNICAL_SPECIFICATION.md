# Technical Specification & Architecture Design Document
## Android Live Delivery & Courier Tracking Engine

**Repository:** `android-live-delivery-tracking-sdk`  
**Author:** Tuncay Avcı  
**Date:** August 2026  
**Tech Stack:** Kotlin 2.0, Jetpack Compose, Multi-Module Clean MVI Architecture, Room, WorkManager, WebSockets, GMS (Google Maps / Places / FCM), HMS (Huawei Location Kit), Yandex MapKit  

---

## 1. System Overview & Business Case

In high-pace e-commerce and express delivery platforms (such as fast grocery and meal delivery services), real-time courier tracking is a high-visibility feature impacting customer satisfaction and retention.

This project delivers a **production-grade Android Tracking SDK** designed to:
1. Provide a unified abstraction layer over multiple map providers (**Google Maps**, **Huawei Maps**, **Yandex MapKit**).
2. Render smooth, 60 FPS courier marker movements using spherical linear interpolation (Lerp).
3. Ensure zero data loss during network tunnel or cellular dead-zone drops using an offline-first **Room + WorkManager** persistence strategy.

---

## 2. Multi-Module Architecture

The project is structured into 5 decoupled Gradle modules:

```
android-live-delivery-tracking-sdk/
├── app/                        # Application entry point, Hilt DI setup, Navigation
├── feature/
│   └── tracking/               # Jetpack Compose UI, TrackingViewModel (MVI), UI State
└── core/
    ├── location-abstraction/   # MapAdapter interface, Gms/Hms/Yandex implementations, Lerp Helper
    ├── network/                # WebSocket client, Telemetry API, FCM push listener
    └── database/               # Room DB, CourierLocationEntity, TelemetryDao
```

---

## 3. Core Component Design

### 3.1 Map Abstraction Pattern (Strategy + Factory)

```kotlin
package com.tuncayavci.tracking.location

import android.content.Context

data class CourierLocation(
    val latitude: Double,
    val longitude: Double,
    val bearing: Float,
    val speedKmh: Float,
    val timestamp: Long
)

data class LatLngPoint(val latitude: Double, val longitude: Double)

interface MapAdapter {
    fun initializeMap(context: Context, container: Any)
    fun updateCourierLocation(location: CourierLocation, animate: Boolean)
    fun setRoutePolyline(points: List<LatLngPoint>)
    fun setMapBounds(courierLocation: CourierLocation, userLocation: CourierLocation)
    fun clearMap()
}
```

### 3.2 Marker Interpolation (Lerp Helper)

```kotlin
package com.tuncayavci.tracking.location

import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import kotlin.math.abs

object MarkerAnimationHelper {
    fun animateMarkerTo(
        marker: Marker,
        targetLatLng: LatLng,
        targetBearing: Float,
        durationMs: Long = 2000
    ) {
        val startLatLng = marker.position
        val startBearing = marker.rotation
        
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = durationMs
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                val fraction = animation.animatedFraction
                val lat = (targetLatLng.latitude - startLatLng.latitude) * fraction + startLatLng.latitude
                val lng = (targetLatLng.longitude - startLatLng.longitude) * fraction + startLatLng.longitude
                val bearing = computeRotation(fraction, startBearing, targetBearing)
                
                marker.position = LatLng(lat, lng)
                marker.rotation = bearing
            }
            start()
        }
    }

    private fun computeRotation(fraction: Float, start: Float, end: Float): Float {
        val normalizeEnd = if (abs(end - start) > 180) end + 360 else end
        return (start + fraction * (normalizeEnd - start)) % 360
    }
}
```

### 3.3 Offline Persistence & WorkManager Sync

```kotlin
package com.tuncayavci.tracking.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_courier_telemetry")
data class CourierLocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: String,
    val latitude: Double,
    val longitude: Double,
    val bearing: Float,
    val timestamp: Long,
    val isSynced: Boolean = false
)
```

---

## 4. WebSocket Payload Contract

```json
{
  "event": "courier_location_update",
  "order_id": "ORD-2026-89421",
  "courier_id": "CR-502",
  "latitude": 41.008238,
  "longitude": 28.978359,
  "bearing": 142.5,
  "speed_kmh": 28.4,
  "timestamp": 1787784000000
}
```

---

## 5. Verification & Testing Strategy

- **Unit Tests:** `TrackingViewModelTest` validates state transitions (`Loading` -> `Success` -> `Error`).
- **Interpolation Tests:** `MarkerAnimationHelperTest` verifies degree wrapping (e.g. 350° to 10° transition).
- **Static Analysis:** Integrated `detekt` and `ktlint` checks enforced via Gradle targets.
