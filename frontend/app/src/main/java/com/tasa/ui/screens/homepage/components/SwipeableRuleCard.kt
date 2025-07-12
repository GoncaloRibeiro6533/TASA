package com.tasa.ui.screens.homepage.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tasa.R
import com.tasa.domain.Event
import com.tasa.domain.RuleEvent
import com.tasa.domain.RuleLocation
import com.tasa.domain.RuleLocationTimeless
import java.time.LocalDateTime

data class SwipeMeta(
    val backgroundColor: Color,
    val icon: ImageVector,
    val alignment: Alignment,
    val label: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SwipeableRuleCard(
    rule: T,
    modifier: Modifier = Modifier,
    onEdit: ((T) -> Unit)? = null,
    onDelete: (T) -> Unit,
    dismissContent: @Composable (T) -> Unit,
    enableEdit: Boolean = true,
    enableDelete: Boolean = true,
) {
    val dismissState =
        rememberSwipeToDismissBoxState(
            confirmValueChange = { dismissValue ->
                when (dismissValue) {
                    SwipeToDismissBoxValue.EndToStart -> {
                        onDelete(rule)
                        false
                    }
                    SwipeToDismissBoxValue.StartToEnd -> {
                        onEdit?.invoke(rule)
                        false
                    }
                    else -> false
                }
            },
        )

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            dismissState.reset()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = enableEdit && onEdit != null,
        enableDismissFromEndToStart = enableDelete,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val meta =
                when (direction) {
                    SwipeToDismissBoxValue.StartToEnd ->
                        SwipeMeta(
                            backgroundColor = MaterialTheme.colorScheme.primary,
                            icon = Icons.Default.Edit,
                            alignment = Alignment.CenterStart,
                            label = stringResource(R.string.edit),
                        )
                    SwipeToDismissBoxValue.EndToStart ->
                        SwipeMeta(
                            backgroundColor = MaterialTheme.colorScheme.error,
                            icon = Icons.Default.Delete,
                            alignment = Alignment.CenterEnd,
                            label = stringResource(R.string.delete),
                        )
                    else -> return@SwipeToDismissBox
                }

            Box(
                modifier =
                    modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(meta.backgroundColor)
                        .padding(horizontal = 20.dp),
                contentAlignment = meta.alignment,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (meta.alignment == Alignment.CenterEnd) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    Icon(
                        imageVector = meta.icon,
                        contentDescription = meta.label,
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = meta.label,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    if (meta.alignment == Alignment.CenterStart) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        },
        content = {
            dismissContent(rule)
        },
    )
}

@Composable
fun SwipeableRuleCardEvent(
    rule: RuleEvent,
    onEdit: (RuleEvent) -> Unit,
    onDelete: (RuleEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    SwipeableRuleCard(
        rule = rule,
        onEdit = onEdit,
        onDelete = onDelete,
        dismissContent = { RuleCardEvent(rule = it) },
        modifier = modifier,
    )
}

@Composable
fun SwipeableRuleCardLocation(
    rule: RuleLocation,
    onEdit: (RuleLocation) -> Unit,
    onDelete: (RuleLocation) -> Unit,
    modifier: Modifier = Modifier,
) {
    SwipeableRuleCard(
        rule = rule,
        onEdit = onEdit,
        onDelete = onDelete,
        dismissContent = { RuleCardLocation(rule = it) },
        modifier = modifier,
    )
}

@Composable
fun SwipeableRuleCardLocationTimeless(
    rule: RuleLocationTimeless,
    onDelete: (RuleLocationTimeless) -> Unit,
    modifier: Modifier = Modifier,
) {
    SwipeableRuleCard(
        rule = rule,
        onEdit = null,
        onDelete = onDelete,
        dismissContent = { RuleCardLocationTimeless(rule = it) },
        modifier = modifier,
        enableEdit = false,
        enableDelete = true,
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
                    eventId = 1,
                    title = "Sample Event",
                ),
        )

    SwipeableRuleCardEvent(
        rule = sampleRule,
        onEdit = { editedRule -> println("Edited: $editedRule") },
        onDelete = { deletedRule -> println("Deleted: $deletedRule") },
    )
}
