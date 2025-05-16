package com.tasa.newlocation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tasa.ui.NavigationHandlers
import com.tasa.ui.TopBar
import org.osmdroid.util.GeoPoint

@Composable
fun MapScreen(onNavigationBack: () -> Unit) {
    var selectedPoint by remember { mutableStateOf<GeoPoint?>(null) }
    Scaffold(
        topBar = {
            TopBar(NavigationHandlers(onBackRequested = onNavigationBack))
        },
    ) { padding ->
        OSMDroidMap(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
            center = selectedPoint ?: GeoPoint(38.7169, -9.1399),
            onCoordinateSelected = { point ->
                selectedPoint = point
            },
        )
    }
}
