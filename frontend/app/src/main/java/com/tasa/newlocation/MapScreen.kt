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
import com.tasa.ui.components.NavigationHandlers
import com.tasa.ui.components.TopBar
import org.osmdroid.util.GeoPoint
import android.location.Geocoder
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale


@Composable
fun MapScreen(onNavigationBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var showConfirmButton by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var formInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopBar(NavigationHandlers(onBackRequested = onNavigationBack))
        },
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Map in the background
            OSMDroidMap(
                modifier = Modifier.fillMaxSize(),
                center = selectedPoint ?: GeoPoint(38.7169, -9.1399),
                onCoordinateSelected = { point ->
                    selectedPoint = point
                },
            )

            // Search bar and button over the map
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                tonalElevation = 4.dp,
                shadowElevation = 8.dp,
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search for a place") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                try {
                                    val geocoder = Geocoder(context, Locale.getDefault())
                                    val addresses = withContext(Dispatchers.IO) {
                                        geocoder.getFromLocationName(searchQuery, 1)
                                    }
                                    if (!addresses.isNullOrEmpty()) {
                                        val address = addresses[0]
                                        selectedPoint = GeoPoint(address.latitude, address.longitude)
                                    } else {
                                        Log.d("MapScreen", "No results for $searchQuery")
                                    }
                                } catch (e: Exception) {
                                    Log.e("MapScreen", "Geocoding error: ${e.message}")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Search")
                    }
                }
                // 📝 Dialog with form
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text("Submit Details") },
                        text = {
                            Column {
                                Text("Enter info for this location:")
                                OutlinedTextField(
                                    value = formInput,
                                    onValueChange = { formInput = it },
                                    label = { Text("Description") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                Log.d(
                                    "MapScreen",
                                    "Confirmed: ${selectedPoint?.latitude}, ${selectedPoint?.longitude} with note: $formInput"
                                )
                                showDialog = false
                                showConfirmButton = false
                                formInput = ""
                            }) {
                                Text("Submit")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
        }
    }
}

