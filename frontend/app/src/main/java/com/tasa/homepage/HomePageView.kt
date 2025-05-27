package com.tasa.homepage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissDirection
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasa.calendar.toFormattedDate
import com.tasa.domain.Event
import com.tasa.domain.Rule
import com.tasa.domain.RuleEvent
import com.tasa.ui.components.RoundedRectangleWithText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

@Composable
fun TextBox(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun HomePageView(
    rules: StateFlow<List<Rule>>,
    onNavigationToMap: () -> Unit,
    onNavigateToCreateRuleEvent: () -> Unit,
    onNavigationToMyExceptions: () -> Unit,
) {
    val ruleList = rules.collectAsState().value
    val gray = ButtonDefaults.buttonColors(Color.Gray)
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        Text(
            text = "My Rules",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.headlineLarge,
        )
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(300.dp),
            contentAlignment = Alignment.Center,
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            if (ruleList.isEmpty()) {
                Text("Nenhuma regra encontrada.", style = MaterialTheme.typography.bodyLarge)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(ruleList) { rule ->
                        SwipeableRuleCard(
                            rule = rule as RuleEvent,
                            onEdit = { editedRule ->
                            },
                            onDelete = { deletedRule ->
                            },
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            val aspectRatio = 1f // Makes it square
            Button(
                onClick = {},
                modifier =
                    Modifier
                        .weight(1f)
                        .aspectRatio(aspectRatio)
                        .padding(4.dp),
                colors = gray,
                shape = RectangleShape,
            ) {
                TextBox("MY LOCATIONS")
            }
            Button(
                onClick = { onNavigateToCreateRuleEvent() },
                modifier =
                    Modifier
                        .weight(1f)
                        .aspectRatio(aspectRatio)
                        .padding(4.dp),
                colors = gray,
                shape = RectangleShape,
            ) {
                TextBox("MY EVENTS")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Row - 3 smaller square buttons
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { onNavigationToMap() },
                modifier =
                    Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                colors = gray,
                shape = RectangleShape,
            ) {
                TextBox("ADD NEW LOCATION")
            }
            Button(
                onClick = { onNavigationToMyExceptions() },
                modifier =
                    Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                colors = gray,
                shape = RectangleShape,
            ) {
                TextBox("MY EXCEPTIONS")
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeableRuleCard(
    rule: RuleEvent,
    onEdit: (RuleEvent) -> Unit,
    onDelete: (RuleEvent) -> Unit,
) {
    val dismissState = rememberDismissState()

    if (dismissState.isDismissed(DismissDirection.EndToStart)) {
        onDelete(rule)
    } else if (dismissState.isDismissed(DismissDirection.StartToEnd)) {
        onEdit(rule)
    }

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
        background = {
            val direction = dismissState.dismissDirection ?: return@SwipeToDismiss
            val color =
                when (direction) {
                    DismissDirection.StartToEnd -> Color.Blue // Edit
                    DismissDirection.EndToStart -> Color.Red // Delete
                }

            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(color)
                        .padding(16.dp),
                contentAlignment =
                    when (direction) {
                        DismissDirection.StartToEnd -> Alignment.CenterStart
                        DismissDirection.EndToStart -> Alignment.CenterEnd
                    },
            ) {
                Text(
                    text = if (direction == DismissDirection.StartToEnd) "Editar" else "Apagar",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        },
        dismissContent = {
            RuleCardEvent(rule = rule)
        },
    )
}

@Composable
fun HomeActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    ElevatedButton(
        onClick = onClick,
        contentPadding = PaddingValues(8.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = text, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun RuleCard(rule: Rule) {
    if (rule is RuleEvent) {
        RuleCardEvent(rule)
    }
}

@Composable
fun RuleCardEvent(
    rule: RuleEvent,
    active: Boolean = false,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                if (active) {
                    RoundedRectangleWithText(text = "Ativo", backgroundColor = Color.Green)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Início: ${rule.startTime.toFormattedDate()}", style = MaterialTheme.typography.bodyMedium)
            Text("Fim: ${rule.endTime.toFormattedDate()}", style = MaterialTheme.typography.bodyMedium)
            Text("Evento: ${rule.event.title}", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePageViewPreview() {
    val dummyRules: List<Rule> =
        listOf(
            RuleEvent(
                id = 1,
                startTime = LocalDateTime.now(),
                endTime = LocalDateTime.now().plusHours(1),
                event =
                    Event(
                        title = "Reunião de equipa",
                        id = 1,
                        calendarId = 1,
                    ),
            ),
            RuleEvent(
                id = 2,
                startTime = LocalDateTime.now().plusDays(1),
                endTime = LocalDateTime.now().plusDays(1).plusHours(2),
                event =
                    Event(
                        title = "Almoço de negócios",
                        id = 2,
                        calendarId = 1,
                    ),
            ),
        )
    HomePageView(
        rules = MutableStateFlow(dummyRules),
        onNavigationToMap = {},
        onNavigateToCreateRuleEvent = {},
        onNavigationToMyExceptions = {},
    )
}
