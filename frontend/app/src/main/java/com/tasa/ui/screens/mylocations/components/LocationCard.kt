package com.tasa.ui.screens.mylocations.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.tasa.R
import com.tasa.domain.Location
import com.tasa.ui.theme.TasaTheme

@Composable
fun LocationCard(
    location: Location,
    onEdit: (Location) -> Unit,
    onDelete: (Location) -> Unit,
    onSetCreateRuleState: (Location) -> Unit,
    onSetCreateTimelessRuleState: (Location) -> Unit,
) {
    val greenColor = MaterialTheme.colorScheme.primary
    val redColor = MaterialTheme.colorScheme.error
    val name = location.name
    val address = "${location.latitude}, ${location.longitude}"
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = { onDelete(location) },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = redColor,
                        )
                    }

                    IconButton(
                        onClick = { onEdit(location) },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = greenColor,
                        )
                    }
                    Box {
                        IconButton(
                            onClick = { showMenu = !showMenu },
                            modifier = Modifier.size(36.dp),
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Criar regra",
                                tint = greenColor,
                            )
                        }
                        if (showMenu) {
                            Popup(
                                offset = IntOffset(0, 0),
                                properties =
                                    androidx.compose.ui.window.PopupProperties(
                                        focusable = true,
                                        dismissOnBackPress = true,
                                        dismissOnClickOutside = true,
                                    ),
                                alignment = Alignment.BottomEnd,
                                onDismissRequest = { showMenu = false },
                            ) {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(8.dp),
                                    colors =
                                        CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface,
                                        ),
                                    modifier =
                                        Modifier
                                            .padding(end = 8.dp),
                                ) {
                                    Column(
                                        modifier =
                                            Modifier
                                                .padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                    ) {
                                        Text(
                                            stringResource(R.string.create_rule_location),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )

                                        Spacer(Modifier.padding(4.dp))

                                        Row(
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier =
                                                    Modifier
                                                        .clickable {
                                                            showMenu = false
                                                            onSetCreateRuleState(location)
                                                        }
                                                        .padding(8.dp),
                                            ) {
                                                Icon(
                                                    Icons.Default.DateRange,
                                                    contentDescription = "Com Schedule",
                                                    tint = greenColor,
                                                    modifier = Modifier.size(24.dp),
                                                )
                                                Text("Horario", style = MaterialTheme.typography.bodySmall)
                                            }

                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier =
                                                    Modifier
                                                        .clickable {
                                                            showMenu = false
                                                            onSetCreateTimelessRuleState(location)
                                                        }
                                                        .padding(8.dp),
                                            ) {
                                                Icon(
                                                    Icons.Default.Place,
                                                    contentDescription = "Simples",
                                                    tint = greenColor,
                                                    modifier = Modifier.size(24.dp),
                                                )
                                                Text("Simples", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LocationCardPreview() {
    val loc1 =
        Location(
            id = 1,
            name = "Cinema",
            latitude = 38.736946,
            longitude = -9.142685,
            radius = 50.0,
        )

    TasaTheme {
        Column {
            repeat(3) {
                LocationCard(loc1, {}, {}, {}, {})
                Spacer(modifier = Modifier.size(8.dp))
            }
        }
    }
}
