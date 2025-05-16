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
