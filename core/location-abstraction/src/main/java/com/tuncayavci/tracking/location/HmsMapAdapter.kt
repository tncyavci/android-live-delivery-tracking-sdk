package com.tuncayavci.tracking.location

import android.content.Context
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.MapView
import com.huawei.hms.maps.model.BitmapDescriptorFactory
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.LatLngBounds
import com.huawei.hms.maps.model.Marker
import com.huawei.hms.maps.model.MarkerOptions
import com.huawei.hms.maps.model.PolylineOptions
import javax.inject.Inject

/**
 * [MapAdapter] backed by Huawei Map Kit. Selected on devices with HMS Core but no Google Play
 * Services, e.g. Huawei/Honor devices outside the GMS ecosystem (see [MapAdapterModule]).
 */
class HmsMapAdapter
    @Inject
    constructor() : MapAdapter {
        private var huaweiMap: HuaweiMap? = null
        private var courierMarker: Marker? = null

        override fun initializeMap(
            context: Context,
            container: Any,
        ) {
            val mapView =
                container as? MapView
                    ?: error("HmsMapAdapter requires a com.huawei.hms.maps.MapView container")
            mapView.getMapAsync { map -> huaweiMap = map }
        }

        override fun updateCourierLocation(
            location: CourierLocation,
            animate: Boolean,
        ) {
            val map = huaweiMap ?: return
            val target = LatLng(location.latitude, location.longitude)
            val marker = courierMarker

            if (marker == null) {
                courierMarker =
                    map.addMarker(
                        MarkerOptions()
                            .position(target)
                            .rotation(location.bearing)
                            .flat(true)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)),
                    )
                return
            }

            // HMS marker animation follows the same linear-interpolation contract as GMS; only the
            // marker/LatLng types differ between the two vendor SDKs.
            if (animate) {
                animateHmsMarker(marker, target, location.bearing)
            } else {
                marker.position = target
                marker.rotation = location.bearing
            }
        }

        override fun setRoutePolyline(points: List<LatLngPoint>) {
            val map = huaweiMap ?: return
            map.addPolyline(
                PolylineOptions().addAll(points.map { LatLng(it.latitude, it.longitude) }),
            )
        }

        override fun setMapBounds(
            courierLocation: CourierLocation,
            userLocation: CourierLocation,
        ) {
            val map = huaweiMap ?: return
            val bounds =
                LatLngBounds.Builder()
                    .include(LatLng(courierLocation.latitude, courierLocation.longitude))
                    .include(LatLng(userLocation.latitude, userLocation.longitude))
                    .build()
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, MAP_BOUNDS_PADDING_PX))
        }

        override fun clearMap() {
            huaweiMap?.clear()
            courierMarker = null
        }

        private fun animateHmsMarker(
            marker: Marker,
            target: LatLng,
            targetBearing: Float,
        ) {
            val start = marker.position
            val startBearing = marker.rotation
            android.animation.ValueAnimator.ofFloat(0f, 1f).apply {
                duration = MARKER_ANIMATION_DURATION_MS
                addUpdateListener { animation ->
                    val fraction = animation.animatedFraction
                    marker.position =
                        LatLng(
                            (target.latitude - start.latitude) * fraction + start.latitude,
                            (target.longitude - start.longitude) * fraction + start.longitude,
                        )
                    marker.rotation = MarkerAnimationHelper.interpolateBearing(startBearing, targetBearing, fraction)
                }
                start()
            }
        }

        private companion object {
            const val MAP_BOUNDS_PADDING_PX = 128
            const val MARKER_ANIMATION_DURATION_MS = 2000L
        }
    }
