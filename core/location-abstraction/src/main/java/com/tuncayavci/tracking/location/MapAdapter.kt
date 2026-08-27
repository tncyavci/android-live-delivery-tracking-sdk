package com.tuncayavci.tracking.location

import android.content.Context

data class CourierLocation(
    val latitude: Double,
    val longitude: Double,
    val bearing: Float,
    val speedKmh: Float,
    val timestamp: Long,
)

data class LatLngPoint(
    val latitude: Double,
    val longitude: Double,
)

/**
 * Strategy Pattern Interface unifying GMS (Google Maps), HMS (Huawei Maps), and Yandex MapKit.
 */
interface MapAdapter {
    fun initializeMap(
        context: Context,
        container: Any,
    )

    fun updateCourierLocation(
        location: CourierLocation,
        animate: Boolean,
    )

    fun setRoutePolyline(points: List<LatLngPoint>)

    fun setMapBounds(
        courierLocation: CourierLocation,
        userLocation: CourierLocation,
    )

    fun clearMap()
}
