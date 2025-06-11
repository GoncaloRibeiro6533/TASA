package com.tasa.newlocation

import android.content.Context
import android.view.MotionEvent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.Polygon
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OSMDroidMap(onCoordinateSelected: (GeoPoint) -> Unit) {
    val context = LocalContext.current

    AndroidView(factory = {
        // Configure OSMDroid
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE),
        )

        val map = MapView(context)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)

        // Initial position
        val mapController = map.controller
        mapController.setZoom(15.0)
        mapController.setCenter(GeoPoint(38.7169, -9.1399)) // Lisbon

        // Set long press listener to pick point
        map.overlays.add(
            object : Overlay() {
                override fun onLongPress(
                    e: MotionEvent?,
                    mapView: MapView?,
                ): Boolean {
                    e ?: return false
                    mapView ?: return false

                    val projection = mapView.projection
                    val geoPoint = projection.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
                    onCoordinateSelected(geoPoint)
                    return true
                }

                override fun onSingleTapConfirmed(
                    e: MotionEvent?,
                    mapView: MapView?,
                ): Boolean {
                    return false
                }
            },
        )

        map
    })
}

fun drawCircle(center: GeoPoint, radius: Double): Polygon {
    val earthRadius = 6371000.0 // meters
    val lat = center.latitude * Math.PI / 180
    val lon = center.longitude * Math.PI / 180
    val points = mutableListOf<GeoPoint>()

    for (i in 0..360 step 10) {
        val angle = i * Math.PI / 180
        val dx = radius * cos(angle)
        val dy = radius * sin(angle)
        val pointLat = lat + (dy / earthRadius)
        val pointLon = lon + (dx / (earthRadius * cos(lat)))
        val geoPoint = GeoPoint(
            pointLat * 180 / Math.PI,
            pointLon * 180 / Math.PI
        )
        points.add(geoPoint)
    }

    return Polygon().apply {
        this.points = points
        fillPaint.color = 0x3000BFFF // semi-transparent blue
        outlinePaint.color = 0xFF00BFFF.toInt() // deep sky blue
        outlinePaint.strokeWidth = 3f
    }
}

