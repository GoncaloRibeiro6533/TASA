package com.tasa.ui.screens.newLocation.components

import android.content.Context
import android.util.Log
import android.view.ViewTreeObserver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import com.tasa.R
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import kotlin.collections.remove
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OSMDroidMap2(
    modifier: Modifier = Modifier,
    center: GeoPoint = GeoPoint(38.7169, -9.1399),
    currentLocation: GeoPoint? = null,
    onCoordinateSelected: ((GeoPoint) -> Unit)? = null,
    accuracy: Float? = null,
    onMapReady: (() -> Unit),
    selectedPoint: GeoPoint? = null,
    radius: Double? = 100.0,
) {
    val mapViewRef = remember { mutableStateOf<MapView?>(null) }
    val userSelectedMarkerRef = remember { mutableStateOf<Marker?>(null) }
    val currentLocationMarkerRef = remember { mutableStateOf<Marker?>(null) }
    val centeredOnce = remember { mutableStateOf(false) }
    val accuracyCircleRef = remember { mutableStateOf<Polygon?>(null) }
    val circleOverlayRef = remember { mutableStateOf<Polygon?>(null) }

    // 🔁 Mantenha selectedPoint sincronizado para efeitos side-effect
    val selectedPointRef = remember { mutableStateOf(center) }

    // Atualiza marker e círculo sempre que selectedPoint muda
    LaunchedEffect(selectedPoint) {
        selectedPoint?.let { point ->
            selectedPointRef.value = point
            userSelectedMarkerRef.value?.position = point
            mapViewRef.value?.controller?.animateTo(point)
            mapViewRef.value?.invalidate()
        }
    }

    // 🔵 Localização atual
    LaunchedEffect(currentLocation, accuracy) {
        val map = mapViewRef.value ?: return@LaunchedEffect
        val location = currentLocation ?: return@LaunchedEffect
        val radius = accuracy ?: return@LaunchedEffect

        // Atualiza o círculo de precisão
        val circle =
            accuracyCircleRef.value ?: Polygon().apply {
                fillPaint.color = 0x22007AFF
                outlinePaint.color = 0xFF007AFF.toInt()
                strokeWidth = 2f
                isEnabled = true
                map.overlays.add(this)
                accuracyCircleRef.value = this
            }
        circle.setPoints(Polygon.pointsAsCircle(location, radius.toDouble()))

        // Atualiza o marcador de localização atual
        val currentMarker =
            currentLocationMarkerRef.value ?: Marker(map).apply {
                icon = ResourcesCompat.getDrawable(map.context.resources, R.drawable.ic_gps_blue_dot, map.context.theme)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                isDraggable = false
                map.overlays.add(this)
                currentLocationMarkerRef.value = this
            }
        currentMarker.position = location

        map.invalidate()
    }

    // 🔁 Atualiza círculo laranja (selected radius)
    LaunchedEffect(selectedPointRef.value, radius) {
        val map = mapViewRef.value ?: return@LaunchedEffect
        circleOverlayRef.value?.let { map.overlays.remove(it) }

        if (radius != null) {
            val circle = drawCircle(selectedPointRef.value, radius)
            map.overlays.add(circle)
            circleOverlayRef.value = circle
            userSelectedMarkerRef.value?.let { marker ->
                map.overlays.remove(marker)
                map.overlays.add(marker)
            } 
        }

        map.invalidate()
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            Configuration.getInstance().load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                controller.setZoom(15.0)
                controller.setCenter(center)

                val userSelectedMarker =
                    Marker(this).apply {
                        position = center
                        title = "Selected Point"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                overlays.add(userSelectedMarker)
                userSelectedMarkerRef.value = userSelectedMarker
                val accuracyCircle =
                    Polygon().apply {
                        fillPaint.color = 0x22007AFF // azul claro transparente
                        outlinePaint.color = 0xFF007AFF.toInt() // azul forte
                        strokeWidth = 2f
                        isEnabled = true
                        setPoints(Polygon.pointsAsCircle(currentLocation ?: center, accuracy?.toDouble() ?: 30.0))
                    }
                overlays.add(accuracyCircle)
                accuracyCircleRef.value = accuracyCircle
                val currentLocationMarker =
                    Marker(this).apply {
                        position = currentLocation ?: center
                        title = "Current Location"
                        icon = ResourcesCompat.getDrawable(ctx.resources, R.drawable.ic_gps_blue_dot, ctx.theme)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        isDraggable = false
                    }
                overlays.add(currentLocationMarker)
                currentLocationMarkerRef.value = currentLocationMarker

                val mapEventsReceiver =
                    object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            p ?: return false
                            selectedPointRef.value = p
                            userSelectedMarker.position = p
                            controller.animateTo(p)
                            invalidate()
                            onCoordinateSelected?.invoke(p)
                            return true
                        }

                        override fun longPressHelper(p: GeoPoint?): Boolean = false
                    }

                overlays.add(MapEventsOverlay(mapEventsReceiver))
                mapViewRef.value = this

                this.addOnFirstLayoutListener { _, _, _, _, _ ->
                    Log.d("MapView", "Map ready (onFirstLayout)")
                    onMapReady()
                }

                post { onMapReady() }

                viewTreeObserver.addOnGlobalLayoutListener(
                    object : ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            viewTreeObserver.removeOnGlobalLayoutListener(this)
                            onMapReady()
                        }
                    },
                )
            }
        },
    )
}

fun drawCircle(
    center: GeoPoint,
    radius: Double,
): Polygon {
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
        val geoPoint =
            GeoPoint(
                pointLat * 180 / Math.PI,
                pointLon * 180 / Math.PI,
            )
        points.add(geoPoint)
    }

    return Polygon().apply {
        this.points = points
        fillPaint.color = 0x22FF7F50 // orange semi-transparente
        outlinePaint.color = 0xFFFF7F50.toInt() // orange forte
        outlinePaint.strokeWidth = 3f
    }
}
