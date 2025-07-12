package com.tasa.ui.screens.homepage

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasa.R
import com.tasa.domain.*
import com.tasa.ui.screens.homepage.components.*
import com.tasa.ui.screens.rule.EditRuleActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

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
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        HomePageViewHorizontal(
            rules = rules,
            onNavigateToMyLocations = onNavigateToMyLocations,
            onNavigationToMap = onNavigationToMap,
            onNavigateToCreateRuleEvent = onNavigateToCreateRuleEvent,
            onNavigationToMyExceptions = onNavigationToMyExceptions,
            onEdit = onEdit,
            onDelete = onDelete,
        )
    } else {
        HomePageViewPortrait(
            rules = rules,
            onNavigateToMyLocations = onNavigateToMyLocations,
            onNavigationToMap = onNavigationToMap,
            onNavigateToCreateRuleEvent = onNavigateToCreateRuleEvent,
            onNavigationToMyExceptions = onNavigationToMyExceptions,
            onEdit = onEdit,
            onDelete = onDelete,
        )
    }
}

@Composable
fun HomePageViewPortrait(
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
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(10000) // 10 segundos
            currentTime = LocalDateTime.now()
        }
    }

    val filteredRules by remember(ruleList, currentTime) {
        derivedStateOf {
            ruleList.filter { rule ->
                when (rule) {
                    is RuleEvent -> rule.endTime.isAfter(currentTime)
                    else -> true // Mantém outras regras que não são eventos
                }
            }
        }
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
                MaterialTheme.typography.headlineMedium.copy(
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
                filteredRules.filter {
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
    var list by rememberSaveable { mutableStateOf(true) }
    val ruleList = rules.collectAsState().value
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(10000)
            currentTime = LocalDateTime.now()
        }
    }

    val filteredRules by remember(ruleList, currentTime) {
        derivedStateOf {
            ruleList.filter { rule ->
                when (rule) {
                    is RuleEvent -> rule.endTime.isAfter(currentTime)
                    else -> true
                }
            }
        }
    }

    val rulesToShow = remember(filteredRules, list) {
        filteredRules.filter {
            (it is TimelessRule && !list) || (it is TimedRule && list)
        }
    }

    val gray = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    )

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp), // separação manual
    ) {
        // 🔹 Menu lateral
        Column(
            modifier = Modifier
                .width(180.dp).padding(top = 20.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CompactButton(
                label = stringResource(R.string.my_locations),
                onClick = onNavigateToMyLocations,
                colors = gray,
                modifier = Modifier.height(65.dp),
            )
            CompactButton(
                label = stringResource(R.string.my_events),
                onClick = onNavigateToCreateRuleEvent,
                colors = gray,
                modifier = Modifier.height(65.dp),
            )
            CompactButton(
                label = stringResource(R.string.add_new_location),
                onClick = onNavigationToMap,
                colors = gray,
                modifier = Modifier.height(65.dp),
            )
            CompactButton(
                label = stringResource(R.string.my_exceptions),
                onClick = onNavigationToMyExceptions,
                colors = gray,
                modifier = Modifier.height(65.dp),
            )
        }

        // 🔸 Espaço entre menu e conteúdo
        Spacer(modifier = Modifier.width(24.dp))

        // 🔸 Conteúdo principal
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.my_rules),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                ),
                color = MaterialTheme.colorScheme.primary,
            )

            RulesToggleBar(
                isTimedSelected = list,
                onSelectTimed = { list = true },
                onSelectLocation = { list = false },
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 8.dp),
                contentAlignment = Alignment.TopStart,
            ) {
                if (rulesToShow.isEmpty()) {
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
                        items(rulesToShow, key = { it.toString() }) { rule ->
                            when (rule) {
                                is RuleEvent -> SwipeableRuleCardEvent(
                                    rule = rule,
                                    onEdit = { onEdit(it.toRuleEventParcelable()) },
                                    onDelete = onDelete,
                                )

                                is RuleLocation -> SwipeableRuleCardLocation(
                                    rule = rule,
                                    onEdit = {},
                                    onDelete = onDelete,
                                )

                                is RuleLocationTimeless -> SwipeableRuleCardLocationTimeless(
                                    rule,
                                    onDelete = onDelete,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewResponsiveHomePagePortrait() {
    HomePageView(
        rules = MutableStateFlow(sampleRules()),
        onNavigateToMyLocations = {},
        onNavigationToMap = {},
        onNavigateToCreateRuleEvent = {},
        onNavigationToMyExceptions = {},
    )
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun PreviewResponsiveHomePageLandscape() {
    HomePageView(
        rules = MutableStateFlow(sampleRules()),
        onNavigateToMyLocations = {},
        onNavigationToMap = {},
        onNavigateToCreateRuleEvent = {},
        onNavigationToMyExceptions = {},
    )
}

fun sampleRules(): List<Rule> = listOf(
    RuleEvent(
        id = 1,
        startTime = LocalDateTime.now(),
        endTime = LocalDateTime.now().plusHours(1),
        event = Event(1,1, 1, "Reunião")
    ),
    RuleEvent(
        id = 1,
        startTime = LocalDateTime.now(),
        endTime = LocalDateTime.now().plusHours(1),
        event = Event(1,1, 1, "Reunião")
    ),
    RuleEvent(
        id = 1,
        startTime = LocalDateTime.now(),
        endTime = LocalDateTime.now().plusHours(1),
        event = Event(1,1, 1, "Reunião")
    ),
    RuleEvent(
        id = 1,
        startTime = LocalDateTime.now(),
        endTime = LocalDateTime.now().plusHours(1),
        event = Event(1,1, 1, "Reunião")
    ),
    RuleEvent(
        id = 1,
        startTime = LocalDateTime.now(),
        endTime = LocalDateTime.now().plusHours(1),
        event = Event(1,1, 1, "Reunião")
    ),
    RuleEvent(
        id = 1,
        startTime = LocalDateTime.now(),
        endTime = LocalDateTime.now().plusHours(1),
        event = Event(1,1, 1, "Reunião")
    ),
    RuleEvent(
        id = 1,
        startTime = LocalDateTime.now(),
        endTime = LocalDateTime.now().plusHours(1),
        event = Event(1,1, 1, "Reunião")
    ),
    RuleEvent(
        id = 1,
        startTime = LocalDateTime.now(),
        endTime = LocalDateTime.now().plusHours(1),
        event = Event(1,1, 1, "Reunião")
    ),
    RuleLocationTimeless(
        id = 2,
        location = Location(1, "Escritório", 38.7169, -9.1399, 100.0)
    )
)
