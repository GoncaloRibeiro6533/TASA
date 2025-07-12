package com.tasa.ui.screens.newLocation.mapViewStates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.tasa.R
import com.tasa.ui.screens.newLocation.components.SearchBox
import kotlinx.coroutines.flow.StateFlow

@Composable
fun MapViewSearching(
    query: StateFlow<TextFieldValue>,
    onSearch: () -> Unit,
    onWriteSearchBox: (TextFieldValue) -> Unit,
    onCreateLocation: () -> Unit,
    onRecenterMap: () -> Unit,
) {
    var searchQuery = query.collectAsState().value
    SearchBox(
        query = searchQuery,
        onQueryChange = { newValue ->
            onWriteSearchBox(newValue)
        },
        onSearch = {
            onSearch()
        },
        modifier = Modifier.fillMaxWidth(),
    )
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End,
    ) {
        FloatingActionButton(
            onClick = {
                onRecenterMap()
            },
            modifier =
                Modifier
                    .padding(16.dp),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_recenter_icon),
                contentDescription = "Atualizar localização",
            )
        }
        FloatingActionButton(
            onClick = { onCreateLocation() },
            modifier =
                Modifier
                    .padding(16.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Criar localização",
            )
        }
    }
}
