package com.tasa.ui.screens.authentication.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tasa.R

@Composable
fun UsernameTextField(
    username: String,
    onUsernameChangeCallback: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val invalidUsernameMessage = stringResource(R.string.invalid_username)
    val invalidUsername = username.isNotEmpty() && !validateUsername(username)

    AuthenticationTextField(
        label = stringResource(R.string.username_label),
        value = username,
        onValueChange = onUsernameChangeCallback,
        modifier = Modifier.fillMaxWidth().then(modifier),
        required = true,
        maxLength = MAX_USERNAME_LENGTH,
        forbiddenCharacters = listOf(' ', '\n', '\t'),
        errorMessage = if (invalidUsername) invalidUsernameMessage else null,
    )
}
