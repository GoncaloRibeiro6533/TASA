package com.tasa.newlocation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MainScreen() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("OSMDroid + Compose") })
        }
    ) { padding ->
        OSMDroidMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}
