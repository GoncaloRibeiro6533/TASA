package com.tasa.ui.screens.homepage

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasa.R
import com.tasa.domain.Event
import com.tasa.domain.Location
import com.tasa.domain.Rule
import com.tasa.domain.RuleEvent
import com.tasa.domain.RuleLocation
import com.tasa.domain.RuleLocationTimeless
import com.tasa.domain.TimedRule
import com.tasa.domain.TimelessRule
import com.tasa.ui.screens.homepage.components.CompactButton
import com.tasa.ui.screens.homepage.components.RulesToggleBar
import com.tasa.ui.screens.homepage.components.SquareButton
import com.tasa.ui.screens.homepage.components.SwipeableRuleCardEvent
import com.tasa.ui.screens.homepage.components.SwipeableRuleCardLocation
import com.tasa.ui.screens.homepage.components.SwipeableRuleCardLocationTimeless
import com.tasa.ui.screens.rule.EditRuleActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime
import kotlin.collections.filter

const val EVENTS_BUTTON = "events_button"
const val EXCEPTIONS_BUTTON = "exceptions_button"
const val HOME_VIEW = "home_view"
const val LOCATIONS_BUTTON = "location_button"
const val MAP_BUTTON = "map_button"

@Composable
fun HomePageView(
    rules: StateFlow<List<Rule>>,
    onNavigateToMyLocations: () -> Unit,
    onNavigationToMap: () -> Unit,
    onNavigateToCreateRuleEvent: () -> Unit,
    onNavigationToMyExceptions: () -> Unit,
    onEdit: (EditRuleActivity.RuleParcelableEvent) -> Unit = {},
    onDelete: (Rule) -> Unit = {},
) {
    var list by rememberSaveable { mutableStateOf(true) } // true = Timed, false = Location
    var ruleList = rules.collectAsState().value
    LaunchedEffect(10000, ruleList) {
        ruleList =
            ruleList.filter {
                (it is RuleEvent && it.endTime.isBefore(LocalDateTime.now()))
            }
        // TODO order
    }
    val gray =
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .testTag(HOME_VIEW),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        Text(
            text = stringResource(R.string.my_rules),
            style =
                MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                ),
            color = MaterialTheme.colorScheme.primary,
        )

        // TOGGLE BAR
        RulesToggleBar(
            isTimedSelected = list,
            onSelectTimed = { list = true },
            onSelectLocation = { list = false },
        )

        // Rules List
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            val filteredRules =
                ruleList.filter {
                    (it is TimelessRule && !list) || (it is TimedRule && list)
                }

            if (filteredRules.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_rule_found),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(filteredRules, key = { it.toString() }) { rule ->
                        when (rule) {
                            is RuleEvent ->
                                SwipeableRuleCardEvent(
                                    rule = rule,
                                    onEdit = { onEdit(it.toRuleEventParcelable()) },
                                    onDelete = onDelete,
                                )
                            is RuleLocation ->
                                SwipeableRuleCardLocation(
                                    rule = rule,
                                    onEdit = {},
                                    onDelete = onDelete,
                                )
                            is RuleLocationTimeless ->
                                SwipeableRuleCardLocationTimeless(
                                    rule,
                                    onDelete = onDelete,
                                )
                        }
                    }
                }
            }
        }

        // ACTION BUTTONS
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SquareButton(
                    label = stringResource(R.string.my_locations),
                    onClick = onNavigateToMyLocations,
                    modifier =
                        Modifier
                            .weight(1f)
                            .testTag(LOCATIONS_BUTTON),
                    colors = gray,
                )
                SquareButton(
                    label = stringResource(R.string.my_events),
                    onClick = onNavigateToCreateRuleEvent,
                    modifier =
                        Modifier
                            .weight(1f)
                            .testTag(EVENTS_BUTTON),
                    colors = gray,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SquareButton(
                    label = stringResource(R.string.add_new_location),
                    onClick = onNavigationToMap,
                    modifier =
                        Modifier
                            .weight(1f)
                            .testTag(MAP_BUTTON),
                    colors = gray,
                )
                SquareButton(
                    label = stringResource(R.string.my_exceptions),
                    onClick = onNavigationToMyExceptions,
                    modifier =
                        Modifier
                            .weight(1f)
                            .testTag(EXCEPTIONS_BUTTON),
                    colors = gray,
                )
            }
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
                        eventId = 1,
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
                        eventId = 2,
                        calendarId = 1,
                    ),
            ),
            RuleEvent(
                id = 3,
                startTime = LocalDateTime.now().plusDays(2),
                endTime = LocalDateTime.now().plusDays(2).plusHours(3),
                event =
                    Event(
                        title = "Reunião com cliente",
                        id = 3,
                        eventId = 3,
                        calendarId = 1,
                    ),
            ),
            RuleEvent(
                id = 4,
                startTime = LocalDateTime.now().plusDays(3),
                endTime = LocalDateTime.now().plusDays(3).plusHours(4),
                event =
                    Event(
                        title = "Reunião de projeto",
                        id = 4,
                        eventId = 4,
                        calendarId = 1,
                    ),
            ),
            RuleEvent(
                id = 5,
                startTime = LocalDateTime.now().plusDays(4),
                endTime = LocalDateTime.now().plusDays(4).plusHours(5),
                event =
                    Event(
                        title = "Reunião de revisão",
                        id = 5,
                        eventId = 5,
                        calendarId = 1,
                    ),
            ),
            RuleEvent(
                id = 6,
                startTime = LocalDateTime.now().plusDays(5),
                endTime = LocalDateTime.now().plusDays(5).plusHours(6),
                event =
                    Event(
                        title = "Reunião de feedback",
                        id = 6,
                        eventId = 6,
                        calendarId = 1,
                    ),
            ),
            RuleLocation(
                id = 7,
                startTime = LocalDateTime.now().plusDays(6),
                endTime = LocalDateTime.now().plusDays(6).plusHours(2),
                location =
                    Location(
                        id = 1,
                        name = "Escritório",
                        latitude = 38.7169,
                        longitude = -9.1399,
                        radius = 100.0,
                    ),
            ),
        )
    HomePageView(
        rules = MutableStateFlow(dummyRules),
        onNavigationToMap = {},
        onNavigateToCreateRuleEvent = {},
        onNavigationToMyExceptions = {},
        onNavigateToMyLocations = {},
    )
}

@Preview(showBackground = true)
@Composable
fun HomePageViewEmptyPreview() {
    HomePageView(
        rules = MutableStateFlow(emptyList()),
        onNavigationToMap = {},
        onNavigateToCreateRuleEvent = {},
        onNavigationToMyExceptions = {},
        onNavigateToMyLocations = {},
    )
}

@Composable
fun HomePageViewHorizontal(
    rules: StateFlow<List<Rule>>,
    onNavigationToMap: () -> Unit,
    onNavigateToMyLocations: () -> Unit,
    onNavigateToCreateRuleEvent: () -> Unit,
    onNavigationToMyExceptions: () -> Unit,
    onEdit: (EditRuleActivity.RuleParcelableEvent) -> Unit = {},
    onDelete: (Rule) -> Unit = {},
) {
    val ruleList = rules.collectAsState().value
    val gray =
        ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )

    Row(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Left Column for Header and Buttons
        Column(
            modifier =
                Modifier
                    .width(180.dp)
                    .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Compact Header
            Text(
                text = stringResource(R.string.my_rules),
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp,
                    ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            // Smaller Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                CompactButton(
                    label = stringResource(R.string.my_locations),
                    onClick = onNavigateToMyLocations,
                    colors = gray,
                )

                CompactButton(
                    label = stringResource(R.string.my_events),
                    onClick = onNavigateToCreateRuleEvent,
                    colors = gray,
                )

                CompactButton(
                    label = stringResource(R.string.add_new_location),
                    onClick = onNavigationToMap,
                    colors = gray,
                )

                CompactButton(
                    label = stringResource(R.string.my_exceptions),
                    onClick = onNavigationToMyExceptions,
                    colors = gray,
                )
            }
        }

        // Rules List
        Box(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            if (ruleList.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_rule_found),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) {
                    items(ruleList) { rule ->
                        if (rule is RuleEvent) {
                            SwipeableRuleCardEvent(
                                rule = rule,
                                onEdit = { editedRule ->
                                    onEdit(editedRule.toRuleEventParcelable())
                                },
                                onDelete = { deletedRule ->
                                    onDelete(deletedRule)
                                },
                                modifier =
                                    Modifier
                                        .width(280.dp)
                                        .fillMaxHeight(0.9f),
                            )
                        }
                        if (rule is RuleLocation) {
                            SwipeableRuleCardLocation(
                                rule = rule,
                                onEdit = { editedRule ->
                                    // onEdit(editedRule.toRuleEventParcelable())
                                },
                                onDelete = { deletedRule ->
                                    onDelete(deletedRule)
                                },
                                modifier =
                                    Modifier
                                        .width(280.dp)
                                        .fillMaxHeight(0.9f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePageViewHorizontalPreview() {
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
                        eventId = 1,
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
                        eventId = 2,
                        calendarId = 1,
                    ),
            ),
            RuleEvent(
                id = 3,
                startTime = LocalDateTime.now().plusDays(2),
                endTime = LocalDateTime.now().plusDays(2).plusHours(3),
                event =
                    Event(
                        title = "Reunião com cliente",
                        id = 3,
                        eventId = 3,
                        calendarId = 1,
                    ),
            ),
            RuleEvent(
                id = 4,
                startTime = LocalDateTime.now().plusDays(3),
                endTime = LocalDateTime.now().plusDays(3).plusHours(4),
                event =
                    Event(
                        title = "Reunião de projeto",
                        id = 4,
                        eventId = 4,
                        calendarId = 1,
                    ),
            ),
            RuleEvent(
                id = 5,
                startTime = LocalDateTime.now().plusDays(4),
                endTime = LocalDateTime.now().plusDays(4).plusHours(5),
                event =
                    Event(
                        title = "Reunião de revisão",
                        id = 5,
                        eventId = 5,
                        calendarId = 1,
                    ),
            ),
            RuleEvent(
                id = 6,
                startTime = LocalDateTime.now().plusDays(5),
                endTime = LocalDateTime.now().plusDays(5).plusHours(6),
                event =
                    Event(
                        title = "Reunião de feedback",
                        id = 6,
                        eventId = 6,
                        calendarId = 1,
                    ),
            ),
            RuleLocation(
                id = 7,
                startTime = LocalDateTime.now().plusDays(6),
                endTime = LocalDateTime.now().plusDays(6).plusHours(2),
                location =
                    Location(
                        id = 1,
                        name = "Escritório",
                        latitude = 38.7169,
                        longitude = -9.1399,
                        radius = 100.0,
                    ),
            ),
        )
    HomePageViewHorizontal(
        rules = MutableStateFlow(dummyRules),
        onNavigationToMap = {},
        onNavigateToCreateRuleEvent = {},
        onNavigationToMyExceptions = {},
        onNavigateToMyLocations = {},
    )
}

@Preview(showBackground = true, uiMode = Configuration.ORIENTATION_LANDSCAPE)
@Composable
fun HomePageViewHorizontalEmptyPreview() {
    HomePageViewHorizontal(
        rules = MutableStateFlow(emptyList()),
        onNavigationToMap = {},
        onNavigateToCreateRuleEvent = {},
        onNavigationToMyExceptions = {},
        onNavigateToMyLocations = {},
    )
}
