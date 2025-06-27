package com.tasa.ui.screens.about

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tasa.ui.components.NavigationHandlers
import com.tasa.ui.components.TopBar
import com.tasa.ui.theme.TasaTheme

/**
 * Root composable for the about screen, the one that displays information about the app.
 * @param onNavigateBack the callback to be invoked when the user requests to go back to the
 * previous screen
 * @param onSendEmailRequested the callback to be invoked when the user requests to send an email
 * @param onOpenUrlRequested the callback to be invoked when the user requests to open an url
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit = { },
    onSendEmailRequested: (String) -> Unit = { },
    onOpenUrlRequested: (Uri) -> Unit = { },
) {
    TasaTheme {
        Scaffold(
            modifier =
                Modifier
                    .fillMaxSize(),
            topBar = {
                TopBar(NavigationHandlers(onBackRequested = onNavigateBack))
            },
        ) { innerPadding ->
            AboutView(
                innerPadding = innerPadding,
                onSendEmailRequested = onSendEmailRequested,
                onOpenUrlRequested = onOpenUrlRequested,
            )
        }
    }
}
