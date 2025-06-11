package com.tasa.newlocation

import android.preference.PreferenceManager
import android.util.Log
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
import org.osmdroid.views.overlay.Polygon

@Composable
fun OSMDroidMap(
    modifier: Modifier = Modifier,
    center: GeoPoint = GeoPoint(38.7169, -9.1399),
    circleCenter: GeoPoint? = null,
    circleRadius: Double? = null,
    currentLocation: GeoPoint? = null,
    onCoordinateSelected: ((GeoPoint) -> Unit)? = null,
) {
    // Remember MapView across recompositions
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }
    val circleOverlayRef = remember { mutableStateOf<Polygon?>(null) }
    val deviceLocationMarkerRef = remember { mutableStateOf<Marker?>(null) }

    val userMarkerRef = remember { mutableStateOf<Marker?>(null) }
    val locationMarkerRef = remember { mutableStateOf<Marker?>(null) }

    LaunchedEffect(mapViewRef.value, center, circleCenter, circleRadius, currentLocation) {
        val map = mapViewRef.value ?: return@LaunchedEffect

        // Atualizar posição do marcador selecionado
        userMarkerRef.value?.position = center
        map.controller.animateTo(center)

        // Atualizar ou criar marcador de localização do dispositivo
        if (currentLocation != null) {
            if (deviceLocationMarkerRef.value == null) {
                val deviceMarker =
                    Marker(map).apply {
                        position = currentLocation
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        title = "Localização atual"
                        icon = map.context.getDrawable(android.R.drawable.presence_online)
                    }
                map.overlays.add(deviceMarker)
                deviceLocationMarkerRef.value = deviceMarker
            } else {
                deviceLocationMarkerRef.value?.position = currentLocation
            }
        }

        // Atualizar círculo
        circleOverlayRef.value?.let { map.overlays.remove(it) }
        circleOverlayRef.value = null
        if (circleCenter != null && circleRadius != null) {
            val newCircle = drawCircle(circleCenter, circleRadius)
            map.overlays.add(newCircle)
            circleOverlayRef.value = newCircle
        }

        map.invalidate()
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

                val userMarker =
                    Marker(this).apply {
                        position = center
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        title = "Selected Point"
                    }
                overlays.add(userMarker)
                userMarkerRef.value = userMarker

                val mapEventsReceiver =
                    object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean = false

                        override fun longPressHelper(p: GeoPoint?): Boolean {
                            p ?: return false
                            Log.d("MapEvents", "Long pressed at: ${p.latitude}, ${p.longitude}")
                            // Move the marker
                            userMarker.position = p
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
                // markerRef.value = marker

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
