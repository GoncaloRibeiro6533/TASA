package com.tasa.ui.screens.calendar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tasa.R
import com.tasa.domain.CalendarEvent

const val EVENT_CARD = "event_card"

@Composable
fun CalendarEventCard(
    event: CalendarEvent,
    onSelected: (CalendarEvent) -> Unit,
) {
    val typography = MaterialTheme.typography

    Card(
        modifier =
            Modifier.Companion
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(20.dp))
                .shadow(8.dp, androidx.compose.foundation.shape.RoundedCornerShape(20.dp))
                .testTag(EVENT_CARD),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    MaterialTheme.colorScheme.surface,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Column(
            modifier =
                Modifier.Companion
                    .background(
                        MaterialTheme.colorScheme.surface,
                    )
                    .padding(20.dp),
        ) {
            Row(
                verticalAlignment = Alignment.Companion.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.Companion.fillMaxWidth(),
            ) {
                Text(
                    text = event.title.ifBlank { stringResource(R.string.no_title) },
                    style = typography.titleMedium.copy(fontWeight = FontWeight.Companion.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(modifier = Modifier.Companion.height(12.dp))
            Row(
                verticalAlignment = Alignment.Companion.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.Companion.fillMaxWidth(),
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.start) + ": ${event.getFormattedStartTime("HH:mm")}",
                        style = typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(R.string.end) + ": ${event.getFormattedEndTime("HH:mm")}",
                        style = typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column {
                    Button(
                        onClick = { onSelected(event) },
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.Companion.width(4.dp))
                        Text(stringResource(R.string.rule))
                    }
                }
            }
        }
    }
}
