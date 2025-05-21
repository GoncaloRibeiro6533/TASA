package com.tasa.homepage

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasa.calendar.toFormattedDate
import com.tasa.domain.Rule
import com.tasa.domain.RuleEvent
import kotlinx.coroutines.flow.StateFlow

@Composable
fun HomePageView(
    rules: StateFlow<List<Rule>>,
    onNavigationToMap: () -> Unit,
    onNavigationToNewEvent: () -> Unit,
    onNavigationToMyEvents: () -> Unit,
) {
    HomePageLayout(rules, onNavigationToMap, onNavigationToNewEvent, onNavigationToMyEvents)
}

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
fun HomePageLayout(
    rules: StateFlow<List<Rule>>,
    onNavigationToMap: () -> Unit,
    onNavigationToNewEvent: () -> Unit,
    onNavigationToMyEvents: () -> Unit,
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
                        RuleCard(rule)
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
                onClick = { onNavigationToMyEvents() },
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
                onClick = {},
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
            Button(
                onClick = { onNavigationToNewEvent() },
                modifier =
                    Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                colors = gray,
                shape = RectangleShape,
            ) {
                TextBox("ADD NEW EVENT")
            }
        }
    }
}

@Composable
fun HomeActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
fun RuleCardEvent(rule: RuleEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Evento", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Início: ${rule.startTime.toFormattedDate()}", style = MaterialTheme.typography.bodyMedium)
            Text("Fim: ${rule.endTime.toFormattedDate()}", style = MaterialTheme.typography.bodyMedium)
            Text("Título: ${rule.event.title}", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
