package com.tuncayavci.tracking.location

import android.content.Context
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import javax.inject.Inject

/**
 * [MapAdapter] backed by Google Maps SDK for Android. Selected when Google Play Services are
 * available on the device (see [MapAdapterModule]).
 */
class GmsMapAdapter
    @Inject
    constructor() : MapAdapter {
        private var googleMap: GoogleMap? = null
        private var courierMarker: com.google.android.gms.maps.model.Marker? = null

        override fun initializeMap(
            context: Context,
            container: Any,
        ) {
            val mapView =
                container as? MapView
                    ?: error("GmsMapAdapter requires a com.google.android.gms.maps.MapView container")
            mapView.getMapAsync { map -> googleMap = map }
        }

        override fun updateCourierLocation(
            location: CourierLocation,
            animate: Boolean,
        ) {
            val map = googleMap ?: return
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

            if (animate) {
                MarkerAnimationHelper.animateMarkerTo(marker, target, location.bearing)
            } else {
                marker.position = target
                marker.rotation = location.bearing
            }
        }

        override fun setRoutePolyline(points: List<LatLngPoint>) {
            val map = googleMap ?: return
            map.addPolyline(
                PolylineOptions().addAll(points.map { LatLng(it.latitude, it.longitude) }),
            )
        }

        override fun setMapBounds(
            courierLocation: CourierLocation,
            userLocation: CourierLocation,
        ) {
            val map = googleMap ?: return
            val bounds =
                LatLngBounds.Builder()
                    .include(LatLng(courierLocation.latitude, courierLocation.longitude))
                    .include(LatLng(userLocation.latitude, userLocation.longitude))
                    .build()
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, MAP_BOUNDS_PADDING_PX))
        }

        override fun clearMap() {
            googleMap?.clear()
            courierMarker = null
        }

        private companion object {
            const val MAP_BOUNDS_PADDING_PX = 128
        }
    }
