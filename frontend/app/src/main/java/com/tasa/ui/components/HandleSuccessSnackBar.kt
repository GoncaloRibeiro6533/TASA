package com.tasa.ui.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.flow.StateFlow

@Composable
fun HandleSuccessSnackbar(
    snackbarHostState: SnackbarHostState,
    messageFlow: StateFlow<Int?>,
    onMessageConsumed: () -> Unit,
    duration: SnackbarDuration = SnackbarDuration.Short,
) {
    val successMessage = messageFlow.collectAsState().value

    if (successMessage != null) {
        val messageText = stringResource(id = successMessage)

        LaunchedEffect(messageText) {
            snackbarHostState.showSnackbar(
                message = messageText,
                duration = duration,
            )
            onMessageConsumed()
        }
    }
}
