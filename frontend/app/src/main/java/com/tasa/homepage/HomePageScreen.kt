package com.tasa.homepage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.tooling.preview.Preview
import com.tasa.ui.TopBar
import com.tasa.ui.theme.TasaTheme

@Composable
fun HomePageScreen(
    onNavigationToMap: () -> Unit,
    onNavigationToNewEvent: () -> Unit,
    onNavigateToMyEvents: () -> Unit
) {
    TasaTheme {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            topBar = {
                TopBar()
            }

        ) { innerPadding ->
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)

            ) {

                HomePageView(onNavigationToMap, onNavigationToNewEvent, onNavigateToMyEvents)

            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomePageScreenPreview() {
    HomePageScreen({}, {}, {})
}