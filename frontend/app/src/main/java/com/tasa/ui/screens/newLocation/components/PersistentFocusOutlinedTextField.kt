package com.tasa.ui.screens.newLocation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.TextFieldValue
import com.tasa.ui.screens.newLocation.MapsScreenState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PersistentFocusOutlinedTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    state: StateFlow<MapsScreenState>,
) {
    val focusRequester = remember { FocusRequester() }

    OutlinedTextField(
        value = value,
        onValueChange = { updated ->
            onValueChange(updated)
        },
        label = { Text(label) },
        modifier =
            modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
        singleLine = true,
    )

    LaunchedEffect(Unit) {
        if (state.value is MapsScreenState.SuccessSearching) {
            focusRequester.requestFocus()
        }
    }
}



/*){
    PersistentFocusOutlinedTextField(
        value = searchQuery,
        onValueChange = { onEditSearchBox(it) },
        label = "Search for a place",
        modifier = Modifier.fillMaxWidth(),
        state = state,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Button(
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.search))
    }
}*/