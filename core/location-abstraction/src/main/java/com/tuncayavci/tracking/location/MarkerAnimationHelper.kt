package com.tuncayavci.tracking.location

import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import kotlin.math.abs

/**
 * Linear interpolation helper for smooth 60 FPS courier marker movement between two
 * consecutive WebSocket telemetry ticks.
 */
object MarkerAnimationHelper {
    fun animateMarkerTo(
        marker: Marker,
        targetLatLng: LatLng,
        targetBearing: Float,
        durationMs: Long = 2000,
    ) {
        val startLatLng = marker.position
        val startBearing = marker.rotation

        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = durationMs
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                val fraction = animation.animatedFraction
                marker.position = interpolatePosition(startLatLng, targetLatLng, fraction)
                marker.rotation = interpolateBearing(startBearing, targetBearing, fraction)
            }
            start()
        }
    }

    /**
     * Pure function extracted so the interpolation math can be unit tested without an
     * Android [ValueAnimator] runtime.
     */
    internal fun interpolatePosition(
        start: LatLng,
        target: LatLng,
        fraction: Float,
    ): LatLng {
        val lat = (target.latitude - start.latitude) * fraction + start.latitude
        val lng = (target.longitude - start.longitude) * fraction + start.longitude
        return LatLng(lat, lng)
    }

    /**
     * Interpolates bearing along the shorter angular path so a courier turning from, say,
     * 350° to 10° sweeps 20° through 360°/0° instead of spinning the marker the long way round.
     */
    internal fun interpolateBearing(
        start: Float,
        end: Float,
        fraction: Float,
    ): Float {
        val normalizedEnd = if (abs(end - start) > 180) end + 360 else end
        return (start + fraction * (normalizedEnd - start)) % 360
    }
}
