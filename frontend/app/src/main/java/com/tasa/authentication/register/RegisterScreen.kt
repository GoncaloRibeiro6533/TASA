package com.tasa.authentication.register

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tasa.ui.NavigationHandlers
import com.tasa.ui.TopBar
import com.tasa.ui.theme.TasaTheme

@Composable
fun RegisterScreen(onNavigationBack: () -> Unit = { }) {
    TasaTheme {
        Scaffold(
            modifier =
                Modifier
                    .fillMaxSize(),
            topBar = { TopBar(NavigationHandlers(onBackRequested = onNavigationBack)) },
        ) { innerPadding ->

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
            ) {
                RegisterView(
                    onSubmit = { _, _, _ -> },
                )
            }
        }
    }
}
