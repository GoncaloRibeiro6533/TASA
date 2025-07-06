package com.tasa.ui.screens.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasa.R
import com.tasa.ui.components.ErrorAlert
import com.tasa.ui.components.NavigationHandlers
import com.tasa.ui.components.TopBar
import com.tasa.ui.theme.TasaTheme

data class MenuItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

@Composable
fun MenuScreen(
    viewModel: MenuViewModel,
    menuItems: List<MenuItem>,
    onNavigateBack: () -> Unit,
    onLogoutIntent: () -> Unit,
) {
    val state = viewModel.state.collectAsState().value
    TasaTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (state is MenuScreenState.Idle || state is MenuScreenState.Error) {
                    TopBar(NavigationHandlers(onBackRequested = onNavigateBack))
                }
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            ) {
                HorizontalDivider()
                when (state) {
                    is MenuScreenState.Idle -> {
                        LazyColumn {
                            items(menuItems) { item ->
                                MenuItemView(item)
                                HorizontalDivider()
                            }
                        }
                    }
                    is MenuScreenState.LoggingOut -> {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = stringResource(R.string.logging_out),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                    is MenuScreenState.LoggedOut -> {}
                    is MenuScreenState.Error -> {
                        ErrorAlert(
                            title = stringResource(R.string.error),
                            message = state.error.message,
                            buttonText = stringResource(R.string.Ok),
                            onDismiss = {
                                viewModel.logout()?.invokeOnCompletion {
                                    onLogoutIntent()
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuItemView(item: MenuItem) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = { item.onClick() })
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            modifier = Modifier.width(24.dp),
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = item.title,
                style = TextStyle(fontSize = 18.sp),
            )
        }
    }
}

@Preview
@Composable
fun MenuItemViewPreview() {
    MenuItemView(
        MenuItem("About", "about screen", Icons.Default.Info, { }),
    )
}
