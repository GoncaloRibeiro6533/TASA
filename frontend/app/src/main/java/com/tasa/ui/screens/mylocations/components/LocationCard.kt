package com.tasa.ui.screens.mylocations.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tasa.domain.Location
import com.tasa.ui.theme.TasaTheme

@Composable
fun LocationCard(
    location: Location,
    onEdit: (Location) -> Unit,
    onDelete: (Location) -> Unit,
    onSetCreateRuleState: (Location) -> Unit,
) {
    val name = location.name
    val address = "${location.latitude}, ${location.longitude}"

    val greenColor = MaterialTheme.colorScheme.primary
    val redColor = MaterialTheme.colorScheme.error

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(6.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(
                    onClick = { onDelete(location) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar localização",
                        tint = redColor,
                    )
                }

                IconButton(
                    onClick = { onEdit(location) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar localização",
                        tint = greenColor,
                    )
                }

                IconButton(
                    onClick = { onSetCreateRuleState(location) },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Criar regra (Geofence)",
                        tint = greenColor,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = false)
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
            LocationCard(loc1, {}, {}, {})
            Spacer(Modifier.padding(5.dp))
            LocationCard(loc1, {}, {}, {})
            Spacer(Modifier.padding(5.dp))
            LocationCard(loc1, {}, {}, {})
        }
    }
}
