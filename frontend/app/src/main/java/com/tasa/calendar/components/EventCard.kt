package com.tasa.calendar.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tasa.ui.theme.TasaTheme


@Composable
fun EventCard(eventName: String, onAddEvent: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = eventName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { onAddEvent() }) {
                Text("Add Event")
            }
        }
    }
}

@Preview(showBackground = false)
@Composable
fun LocationCardPreview() {


    val event1 = "Casamento"
    val event2 = "Reunião"
    val event3 = "Cinema"

    TasaTheme {
        Column {

            EventCard(event1) {}
            Spacer(Modifier.padding(5.dp))
            EventCard(event2) {}
            Spacer(Modifier.padding(5.dp))
            EventCard(event3) {}
        }
    }
}