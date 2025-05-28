package com.tasa.ui.screens.rule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tasa.domain.Rule
import com.tasa.ui.components.ErrorAlert
import com.tasa.ui.components.LoadingView
import com.tasa.ui.components.NavigationHandlers
import com.tasa.ui.components.TopBar
import com.tasa.ui.theme.TasaTheme
import java.time.LocalDateTime

@Composable
fun EditRuleScreen(
    viewModel: EditRuleViewModel,
    rule: Rule,
    onRuleUpdated: (Rule) -> Unit,
    onRuleUpdate: (LocalDateTime, LocalDateTime) -> Unit,
    onBackPressed: () -> Unit,
    onError: () -> Unit = { },
) {
    TasaTheme {
        Scaffold(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            topBar = {
                TopBar(NavigationHandlers(onBackRequested = onBackPressed))
            },
        ) { innerPadding ->
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
            ) {
                when (val state = viewModel.state.collectAsState().value) {
                    is EditRuleState.Error -> {
                        ErrorAlert(
                            title = "Error",
                            message = state.message,
                            buttonText = "Close",
                            onDismiss = { onError() },
                        )
                    }
                    EditRuleState.Loading -> {
                        LoadingView()
                    }
                    is EditRuleState.Editing -> {
                        EditRuleEventView(
                            rule = rule,
                            onUpdate = { newStartTime, newEndTime ->
                                onRuleUpdate(newStartTime, newEndTime)
                            },
                            onCancel = onBackPressed,
                        )
                    }
                    is EditRuleState.Success -> {
                        onBackPressed()
                    }
                }
            }
        }
    }
}
