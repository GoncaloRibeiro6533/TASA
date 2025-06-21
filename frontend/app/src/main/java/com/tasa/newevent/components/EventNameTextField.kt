package com.tasa.newevent.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tasa.ui.screens.authentication.components.MAX_EVENT_NAME_LENGTH

@Composable
fun EventNameTextField(
    eventName: String,
    onEventNameChangeCallback: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    NewEventTextField(
        label = "Event Name",
        value = eventName,
        onValueChange = onEventNameChangeCallback,
        modifier = Modifier.fillMaxWidth().then(modifier),
        required = true,
        maxLength = MAX_EVENT_NAME_LENGTH,
        forbiddenCharacters = listOf(' ', '\n', '\t'),
    )
}
