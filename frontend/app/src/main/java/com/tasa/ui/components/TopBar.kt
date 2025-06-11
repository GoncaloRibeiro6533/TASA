package com.tasa.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.tasa.R
import com.tasa.ui.theme.TasaTheme

/**
 * Used to aggregate [TopBar] navigation handlers. If a handler is null, the corresponding
 * navigation element is not displayed.
 *
 * @property onBackRequested the callback invoked when the user clicks the back button.
 * @property onAboutRequested the callback invoked when the user clicks the about button.
 */
data class NavigationHandlers(
    val onBackRequested: (() -> Unit)? = null,
    val onAboutRequested: (() -> Unit)? = null,
    val onMenuRequested: (() -> Unit)? = null,
)

const val NavigateBackTestTag = "NavigateBack"
const val MenuButtonTestTag = "MenuButton"
const val AboutButtonTestTag = "AboutButton"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navigation: NavigationHandlers = NavigationHandlers(),
    content: @Composable () -> Unit = {
        Text(
            text = "TASA",
            modifier = Modifier,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight(500),
        )
    },
) {
    TopAppBar(
        title = { content() },
        navigationIcon = {
            if (navigation.onBackRequested != null) {
                IconButton(
                    onClick = navigation.onBackRequested,
                    modifier = Modifier.testTag(NavigateBackTestTag),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.arrow_back_description),
                    )
                }
            } else if (navigation.onMenuRequested != null) {
                IconButton(
                    onClick = navigation.onMenuRequested,
                    modifier = Modifier.testTag(MenuButtonTestTag),
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = stringResource(id = R.string.top_bar_open_menu),
                    )
                }
            }
        },
        actions = {
            if (navigation.onAboutRequested != null) {
                IconButton(
                    onClick = navigation.onAboutRequested,
                    modifier = Modifier.testTag(AboutButtonTestTag),
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(id = R.string.top_bar_navigate_to_about),
                    )
                }
            }
        },
    )
}

@Preview
@Composable
private fun TopBarPreviewBack() {
    TasaTheme {
        TopBar(navigation = NavigationHandlers(onBackRequested = { }))
    }
}

@Preview
@Composable
private fun TopBarPreviewAbout() {
    TasaTheme {
        TopBar(navigation = NavigationHandlers(onAboutRequested = { }))
    }
}

@Preview
@Composable
private fun TopBarPreviewMenu() {
    TasaTheme {
        TopBar(navigation = NavigationHandlers(onMenuRequested = { }))
    }
}
