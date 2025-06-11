package com.tasa.mylocations.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasa.domain.Location
import com.tasa.ui.theme.TasaTheme

@Composable
fun LocationCard(
    location: Location,
    onEdit: (Location) -> Unit,
    onDelete: (Location) -> Unit,
) {
    val name = location.name
    val adress = "${location.latitude}, ${location.longitude}"

    val greenColor = Color(0xFF5CB338)
    val redColor = Color.Red

    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.padding(10.dp))
                Column {
                    Text(text = name)
                    Text(text = adress, fontSize = 10.sp)
                }
            }
            Row(
                modifier = Modifier,
                horizontalArrangement = Arrangement.End,
            ) {
                Spacer(Modifier.width(16.dp))
                IconButton(
                    onClick = { onDelete(location) },
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Decline Invitation",
                        tint = redColor,
                    )
                }

                Spacer(Modifier.width(8.dp))

                IconButton(
                    onClick = { onEdit(location) },
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Accept Invitation",
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
            LocationCard(loc1, {}, {})
            Spacer(Modifier.padding(5.dp))
            LocationCard(loc1, {}, {})
            Spacer(Modifier.padding(5.dp))
            LocationCard(loc1, {}, {})
        }
    }
}
