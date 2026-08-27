package com.tuncayavci.tracking.location

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Test

class MarkerAnimationHelperTest {
    @Test
    fun `interpolatePosition returns start position at fraction zero`() {
        val start = LatLng(41.0, 29.0)
        val target = LatLng(41.1, 29.2)

        val result = MarkerAnimationHelper.interpolatePosition(start, target, fraction = 0f)

        assertEquals(start.latitude, result.latitude, DELTA)
        assertEquals(start.longitude, result.longitude, DELTA)
    }

    @Test
    fun `interpolatePosition returns target position at fraction one`() {
        val start = LatLng(41.0, 29.0)
        val target = LatLng(41.1, 29.2)

        val result = MarkerAnimationHelper.interpolatePosition(start, target, fraction = 1f)

        assertEquals(target.latitude, result.latitude, DELTA)
        assertEquals(target.longitude, result.longitude, DELTA)
    }

    @Test
    fun `interpolatePosition returns midpoint at fraction half`() {
        val start = LatLng(40.0, 28.0)
        val target = LatLng(42.0, 30.0)

        val result = MarkerAnimationHelper.interpolatePosition(start, target, fraction = 0.5f)

        assertEquals(41.0, result.latitude, DELTA)
        assertEquals(29.0, result.longitude, DELTA)
    }

    @Test
    fun `interpolateBearing wraps through 360 for the shorter path`() {
        // 350deg -> 10deg is a 20deg turn through the 0/360 boundary, not a 340deg turn backwards.
        val result = MarkerAnimationHelper.interpolateBearing(start = 350f, end = 10f, fraction = 0.5f)

        assertEquals(0f, result, DELTA.toFloat())
    }

    @Test
    fun `interpolateBearing wraps correctly in the opposite direction`() {
        // 10deg -> 350deg should also take the short 20deg path (backwards through 0), not +340deg.
        val result = MarkerAnimationHelper.interpolateBearing(start = 10f, end = 350f, fraction = 0.5f)

        assertEquals(0f, ((result + 360) % 360), DELTA.toFloat())
    }

    @Test
    fun `interpolateBearing takes the direct path when under 180 degrees`() {
        val result = MarkerAnimationHelper.interpolateBearing(start = 100f, end = 140f, fraction = 0.5f)

        assertEquals(120f, result, DELTA.toFloat())
    }

    @Test
    fun `interpolateBearing reaches the exact end bearing at fraction one`() {
        val result = MarkerAnimationHelper.interpolateBearing(start = 350f, end = 10f, fraction = 1f)

        assertEquals(10f, result, DELTA.toFloat())
    }

    private companion object {
        const val DELTA = 0.0001
    }
}
