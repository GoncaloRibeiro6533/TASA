package com.tasa.ui.screens.homepage.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

const val RULES_BAR = "rules_bar"

@Composable
fun RulesToggleBar(
    isTimedSelected: Boolean,
    onSelectTimed: () -> Unit,
    onSelectLocation: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag(RULES_BAR),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Rules Timed",
            modifier =
                Modifier
                    .clickable { onSelectTimed() }
                    .padding(8.dp),
            color =
                if (isTimedSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            style = MaterialTheme.typography.labelLarge,
        )
        Spacer(modifier = Modifier.width(24.dp))
        Text(
            text = "Rules Location",
            modifier =
                Modifier
                    .clickable { onSelectLocation() }
                    .padding(8.dp),
            color =
                if (!isTimedSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            style = MaterialTheme.typography.labelLarge,
        )
    }
}
