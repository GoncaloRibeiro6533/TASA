package com.tasa.mylocations

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasa.domain.Location
import com.tasa.mylocations.components.LocationCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun MyLocationsView(
    locations: StateFlow<List<Location>>,
    onEdit: (Location) -> Unit,
    onDelete: (Location) -> Unit,
) {
    val locationsList = locations.collectAsState().value
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "My Locations",
            modifier = Modifier.padding(10.dp),
            fontSize = 30.sp,
        )

        Spacer(Modifier.height(20.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
        ) {
            if (locationsList.isEmpty()) {
                item {
                    Spacer(Modifier.height(250.dp))

                    Text(
                        text = "You don't have any location saved",
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray,
                    )
                }
            } else {
                items(locationsList) { location ->
                    LocationCard(
                        location = location,
                        onEdit = { onEdit(location) },
                        onDelete = { onDelete(location) },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MyLocationsPreview() {
    val loc1 =
        Location(
            id = 1,
            name = "Cinema Centro Comercial Colombo",
            latitude = 38.736946,
            longitude = -9.142685,
            radius = 50.0,
        )
    val loc2 =
        Location(
            1,
            "Teatro Tivoli",
            38.7169,
            -9.1399,
            100.0,
        )
    val loc3 =
        Location(
            3,
            "ISEL",
            38.7169,
            -9.1399,
            100.0,
        )
    val locList = listOf(loc1, loc2, loc3)

    MyLocationsView(MutableStateFlow(locList), {}, {})
}
