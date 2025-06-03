package com.tasa.ui.screens.homepage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasa.domain.Event
import com.tasa.domain.Rule
import com.tasa.domain.RuleEvent
import com.tasa.ui.screens.homepage.components.SquareButton
import com.tasa.ui.screens.homepage.components.SwipeableRuleCard
import com.tasa.ui.screens.rule.EditRuleActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

@Composable
fun HomePageView(
    rules: StateFlow<List<Rule>>,
    onNavigationToMap: () -> Unit,
    onNavigateToCreateRuleEvent: () -> Unit,
    onNavigationToMyExceptions: () -> Unit,
    onEdit: (EditRuleActivity.RuleParcelableEvent) -> Unit = {},
    onDelete: (Rule) -> Unit = {},
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

                                onEdit(editedRule.toRuleEventParcelable())
                            },
                            onDelete = { deletedRule ->
                                onDelete(deletedRule)
                            },
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            SquareButton(
                label = "MY LOCATIONS",
                onClick = {},
                modifier = Modifier.weight(1f),
                colors = gray,
            )

            SquareButton(
                label = "MY EVENTS",
                onClick = onNavigateToCreateRuleEvent,
                modifier = Modifier.weight(1f),
                colors = gray,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            SquareButton(
                label = "ADD NEW LOCATION",
                onClick = onNavigationToMap,
                modifier = Modifier.weight(1f),
                colors = gray,
            )

            SquareButton(
                label = "MY EXCEPTIONS",
                onClick = onNavigationToMyExceptions,
                modifier = Modifier.weight(1f),
                colors = gray,
            )
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
