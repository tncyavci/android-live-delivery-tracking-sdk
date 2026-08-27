package com.tuncayavci.tracking.location

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.geometry.Geometry
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import javax.inject.Inject
import com.yandex.mapkit.map.Map as YandexMap

/**
 * [MapAdapter] backed by Yandex MapKit, the fallback provider for markets/devices where neither
 * Google Play Services nor HMS Core are available (see [MapAdapterModule]).
 */
class YandexMapAdapter
    @Inject
    constructor() : MapAdapter {
        private var yandexMap: YandexMap? = null
        private var mapObjects: MapObjectCollection? = null
        private var courierMarker: PlacemarkMapObject? = null
        private var courierIcon: ImageProvider? = null

        override fun initializeMap(
            context: Context,
            container: Any,
        ) {
            val mapView =
                container as? MapView
                    ?: error("YandexMapAdapter requires a com.yandex.mapkit.mapview.MapView container")
            yandexMap = mapView.mapWindow.map
            mapObjects = mapView.mapWindow.map.mapObjects
            courierIcon = ImageProvider.fromBitmap(buildCourierMarkerBitmap())
        }

        override fun updateCourierLocation(
            location: CourierLocation,
            animate: Boolean,
        ) {
            val objects = mapObjects ?: return
            val icon = courierIcon ?: return
            val target = Point(location.latitude, location.longitude)
            val marker = courierMarker

            if (marker == null) {
                courierMarker =
                    objects.addPlacemark().apply {
                        geometry = target
                        setIcon(icon)
                        direction = location.bearing
                    }
                return
            }

            if (animate) {
                animateMarkerTo(marker, target, location.bearing)
            } else {
                marker.geometry = target
                marker.direction = location.bearing
            }
        }

        override fun setRoutePolyline(points: List<LatLngPoint>) {
            val objects = mapObjects ?: return
            objects.addPolyline(Polyline(points.map { Point(it.latitude, it.longitude) }))
        }

        override fun setMapBounds(
            courierLocation: CourierLocation,
            userLocation: CourierLocation,
        ) {
            val map = yandexMap ?: return
            val south = minOf(courierLocation.latitude, userLocation.latitude)
            val north = maxOf(courierLocation.latitude, userLocation.latitude)
            val west = minOf(courierLocation.longitude, userLocation.longitude)
            val east = maxOf(courierLocation.longitude, userLocation.longitude)

            val boundingBox = BoundingBox(Point(south, west), Point(north, east))
            val target = map.cameraPosition(Geometry.fromBoundingBox(boundingBox))
            map.move(
                CameraPosition(target.target, target.zoom, target.azimuth, target.tilt),
            )
        }

        override fun clearMap() {
            mapObjects?.clear()
            courierMarker = null
        }

        private fun animateMarkerTo(
            marker: PlacemarkMapObject,
            target: Point,
            targetBearing: Float,
        ) {
            val start = marker.geometry
            val startBearing = marker.direction
            ValueAnimator.ofFloat(0f, 1f).apply {
                duration = MARKER_ANIMATION_DURATION_MS
                addUpdateListener { animation ->
                    val fraction = animation.animatedFraction
                    marker.geometry =
                        Point(
                            (target.latitude - start.latitude) * fraction + start.latitude,
                            (target.longitude - start.longitude) * fraction + start.longitude,
                        )
                    marker.direction = MarkerAnimationHelper.interpolateBearing(startBearing, targetBearing, fraction)
                }
                start()
            }
        }

        /** Self-contained default courier marker so this module carries no drawable resources. */
        private fun buildCourierMarkerBitmap(): Bitmap {
            val bitmap = Bitmap.createBitmap(MARKER_SIZE_PX, MARKER_SIZE_PX, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor(MARKER_COLOR) }
            val radius = MARKER_SIZE_PX / 2f
            canvas.drawCircle(radius, radius, radius, paint)
            return bitmap
        }

        private companion object {
            const val MARKER_ANIMATION_DURATION_MS = 2000L
            const val MARKER_SIZE_PX = 48
            const val MARKER_COLOR = "#1A73E8"
        }
    }
