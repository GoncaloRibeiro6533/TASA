package com.tasa.ui.screens.homepage.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tasa.domain.RuleEvent
import com.tasa.domain.RuleLocation
import com.tasa.domain.toFormattedDate
import com.tasa.ui.components.RoundedRectangleWithText

@Composable
fun RuleCardEvent(
    rule: RuleEvent,
    active: Boolean = false,
) {
    Card(
        modifier = Modifier.Companion.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.Companion.padding(16.dp)) {
            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (active) {
                    RoundedRectangleWithText(
                        text = "Ativo",
                        backgroundColor = Color.Companion.Green,
                    )
                }
            }
            Spacer(modifier = Modifier.Companion.height(4.dp))
            Text(
                "Início: ${rule.startTime.toFormattedDate()}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Fim: ${rule.endTime.toFormattedDate()}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text("Evento: ${rule.event.title}", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun RuleCardLocation(
    rule: RuleLocation,
    active: Boolean = false,
) {
    Card(
        modifier = Modifier.Companion.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.Companion.padding(16.dp)) {
            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (active) {
                    RoundedRectangleWithText(
                        text = "Ativo",
                        backgroundColor = Color.Companion.Green,
                    )
                }
            }
            Spacer(modifier = Modifier.Companion.height(4.dp))
            Text(
                "Início: ${rule.startTime.toFormattedDate()}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                "Fim: ${rule.endTime.toFormattedDate()}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text("Localização: ${rule.location.name}", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
