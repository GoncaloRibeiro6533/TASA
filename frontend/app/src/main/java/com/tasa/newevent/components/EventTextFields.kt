package com.tasa.newevent.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp




private const val EVENT_NAME_TO_DATE_PADDING = 8
private const val TEXT_FIELD_WIDTH_FACTOR = 0.6f

@Composable
fun EventTextFields(
    eventName: String,

    onEventNameChangeCallback: (String) -> Unit,

    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(TEXT_FIELD_WIDTH_FACTOR)) {


        EventNameTextField(
            eventName = eventName,
            onEventNameChangeCallback = onEventNameChangeCallback,
            modifier = Modifier.padding(bottom =EVENT_NAME_TO_DATE_PADDING.dp).fillMaxWidth()
        )

    }
}

@Preview(showBackground = true)
@Composable
fun EventTextFieldsPreview() {
    EventTextFields("", {})
}