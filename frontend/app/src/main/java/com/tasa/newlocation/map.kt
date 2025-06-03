package com.tasa.newlocation

import android.preference.PreferenceManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@Composable
fun OSMDroidMap(
    modifier: Modifier = Modifier,
    center: GeoPoint = GeoPoint(38.7169, -9.1399), // Lisbon
    onCoordinateSelected: ((GeoPoint) -> Unit)? = null,
) {
    // Remember MapView across recompositions
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }
    val markerRef = remember { mutableStateOf<Marker?>(null) }

    // Update the map when center changes
    LaunchedEffect(center) {
        mapViewRef.value?.let { map ->
            markerRef.value?.position = center
            map.controller.animateTo(center)
            map.invalidate()
        }
    }
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)

                controller.setZoom(15.0)
                controller.setCenter(center)

                val marker =
                    Marker(this).apply {
                        position = center
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Selected Point"
                    }
                overlays.add(marker)

                val mapEventsReceiver =
                    object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean = false

                        override fun longPressHelper(p: GeoPoint?): Boolean {
                            p ?: return false

                            // Move the marker
                            marker.position = p
                            controller.animateTo(p)
                            invalidate() // redraw the map

                            // Call the callback
                            onCoordinateSelected?.invoke(p)
                            return true
                        }
                    }

                val eventsOverlay = MapEventsOverlay(mapEventsReceiver)
                overlays.add(eventsOverlay)

                mapViewRef.value = this
                markerRef.value = marker

                /* Marker
                val marker = Marker(this)
                marker.position = center
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.title = "Center Marker"
                overlays.add(marker)

                 */
            }
        },
    )
}
