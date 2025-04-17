package com.tasa.newlocation

import android.preference.PreferenceManager
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


@Composable
fun OSMDroidMap(
    modifier: Modifier = Modifier,
    center: GeoPoint = GeoPoint(38.7169, -9.1399) // Lisbon
) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)

                controller.setZoom(15.0)
                controller.setCenter(center)

                // Marker
                val marker = Marker(this)
                marker.position = center
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.title = "Center Marker"
                overlays.add(marker)
            }
        }
    )
}
