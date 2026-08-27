package com.tuncayavci.tracking.feature.tracking.components

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.tuncayavci.tracking.location.GmsMapAdapter
import com.tuncayavci.tracking.location.HmsMapAdapter
import com.tuncayavci.tracking.location.MapAdapter
import com.tuncayavci.tracking.location.YandexMapAdapter
import com.yandex.mapkit.MapKitFactory

/**
 * Bridges Compose to whichever native vendor `MapView` the injected [MapAdapter] actually needs
 * (GMS/HMS/Yandex all ship their own View subclass with its own lifecycle contract), so screens
 * built on top of this SDK never have to know which provider is active on the current device.
 */
@Composable
fun CourierMapView(
    mapAdapter: MapAdapter,
    modifier: Modifier = Modifier,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val nativeMapViewState = remember(mapAdapter) { mutableStateOf<View?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val mapView: View =
                when (mapAdapter) {
                    is GmsMapAdapter -> com.google.android.gms.maps.MapView(context).also { it.onCreate(null) }
                    is HmsMapAdapter -> com.huawei.hms.maps.MapView(context).also { it.onCreate(null) }
                    is YandexMapAdapter -> com.yandex.mapkit.mapview.MapView(context)
                    else -> error("Unsupported MapAdapter implementation: ${mapAdapter::class.java.name}")
                }
            mapAdapter.initializeMap(context, mapView)
            nativeMapViewState.value = mapView
            mapView
        },
    )

    DisposableEffect(lifecycleOwner, mapAdapter) {
        val observer =
            LifecycleEventObserver { _, event ->
                val nativeMapView = nativeMapViewState.value
                when (event) {
                    Lifecycle.Event.ON_START ->
                        if (mapAdapter is YandexMapAdapter) {
                            MapKitFactory.getInstance().onStart()
                            (nativeMapView as? com.yandex.mapkit.mapview.MapView)?.onStart()
                        }
                    Lifecycle.Event.ON_RESUME -> {
                        (nativeMapView as? com.google.android.gms.maps.MapView)?.onResume()
                        (nativeMapView as? com.huawei.hms.maps.MapView)?.onResume()
                    }
                    Lifecycle.Event.ON_PAUSE -> {
                        (nativeMapView as? com.google.android.gms.maps.MapView)?.onPause()
                        (nativeMapView as? com.huawei.hms.maps.MapView)?.onPause()
                    }
                    Lifecycle.Event.ON_STOP ->
                        if (mapAdapter is YandexMapAdapter) {
                            (nativeMapView as? com.yandex.mapkit.mapview.MapView)?.onStop()
                            MapKitFactory.getInstance().onStop()
                        }
                    else -> Unit
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            val nativeMapView = nativeMapViewState.value
            (nativeMapView as? com.google.android.gms.maps.MapView)?.onDestroy()
            (nativeMapView as? com.huawei.hms.maps.MapView)?.onDestroy()
            mapAdapter.clearMap()
        }
    }
}
