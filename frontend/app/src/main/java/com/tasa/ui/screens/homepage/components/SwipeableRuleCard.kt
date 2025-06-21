package com.tasa.ui.screens.homepage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tasa.domain.Event
import com.tasa.domain.RuleEvent
import com.tasa.domain.RuleLocation
import java.time.LocalDateTime

data class SwipeMeta(
    val backgroundColor: Color,
    val icon: ImageVector,
    val alignment: Alignment,
    val label: String,
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeableRuleCardEvent(
    rule: RuleEvent,
    onEdit: (RuleEvent) -> Unit,
    onDelete: (RuleEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState =
        rememberDismissState(
            confirmStateChange = { dismissValue ->
                when (dismissValue) {
                    DismissValue.DismissedToStart -> {
                        onDelete(rule)
                        true
                    }

                    DismissValue.DismissedToEnd -> {
                        onEdit(rule)
                        true
                    }

                    else -> false
                }
            },
        )

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
        background = {
            val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
            val meta =
                when (direction) {
                    DismissDirection.StartToEnd ->
                        SwipeMeta(
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            icon = Icons.Default.Edit,
                            alignment = Alignment.CenterStart,
                            label = "Editar",
                        )

                    DismissDirection.EndToStart ->
                        SwipeMeta(
                            backgroundColor = MaterialTheme.colorScheme.error,
                            icon = Icons.Default.Delete,
                            alignment = Alignment.CenterEnd,
                            label = "Apagar",
                        )
                }

            Box(
                modifier =
                    modifier
                        .fillMaxSize()
                        .background(meta.backgroundColor)
                        .padding(horizontal = 20.dp),
                contentAlignment = meta.alignment,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (meta.alignment == Alignment.CenterEnd) Spacer(modifier = modifier.weight(1f))
                    Icon(
                        imageVector = meta.icon,
                        contentDescription = meta.label,
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(modifier = modifier.width(8.dp))
                    Text(
                        text = meta.label,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    if (meta.alignment == Alignment.CenterStart) Spacer(modifier = modifier.weight(1f))
                }
            }
        },
        dismissContent = {
            RuleCardEvent(rule = rule)
        },
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeableRuleCardLocation(
    rule: RuleLocation,
    onEdit: (RuleLocation) -> Unit,
    onDelete: (RuleLocation) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState =
        rememberDismissState(
            confirmStateChange = { dismissValue ->
                when (dismissValue) {
                    DismissValue.DismissedToStart -> {
                        onDelete(rule)
                        true
                    }

                    DismissValue.DismissedToEnd -> {
                        // onEdit(rule)
                        true
                    }

                    else -> false
                }
            },
        )

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
        background = {
            val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
            val meta =
                when (direction) {
                    DismissDirection.StartToEnd ->
                        SwipeMeta(
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            icon = Icons.Default.Edit,
                            alignment = Alignment.CenterStart,
                            label = "Editar",
                        )

                    DismissDirection.EndToStart ->
                        SwipeMeta(
                            backgroundColor = MaterialTheme.colorScheme.error,
                            icon = Icons.Default.Delete,
                            alignment = Alignment.CenterEnd,
                            label = "Apagar",
                        )
                }

            Box(
                modifier =
                    modifier
                        .fillMaxSize()
                        .background(meta.backgroundColor)
                        .padding(horizontal = 20.dp),
                contentAlignment = meta.alignment,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (meta.alignment == Alignment.CenterEnd) Spacer(modifier = modifier.weight(1f))
                    Icon(
                        imageVector = meta.icon,
                        contentDescription = meta.label,
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(modifier = modifier.width(8.dp))
                    Text(
                        text = meta.label,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    if (meta.alignment == Alignment.CenterStart) Spacer(modifier = modifier.weight(1f))
                }
            }
        },
        dismissContent = {
            RuleCardLocation(rule = rule)
        },
    )
}

@Preview(showBackground = true)
@Composable
fun SwipeableRuleCardPreview() {
    val sampleRule =
        RuleEvent(
            id = 1,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now().plusHours(1),
            event =
                Event(
                    id = 1,
                    calendarId = 1,
                    title = "Sample Event",
                ),
        )

    SwipeableRuleCardEvent(
        rule = sampleRule,
        onEdit = { editedRule -> println("Edited: $editedRule") },
        onDelete = { deletedRule -> println("Deleted: $deletedRule") },
    )
}
