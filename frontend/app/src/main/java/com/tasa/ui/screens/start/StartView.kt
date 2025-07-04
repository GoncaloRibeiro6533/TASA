package com.tasa.ui.screens.start

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasa.R

@Composable
fun StartView(
    modifier: Modifier = Modifier,
    onLoginRequested: () -> Unit,
    onRegisterRequested: () -> Unit,
    onContinueWithoutAccount: () -> Unit,
) {
    val orientation = LocalConfiguration.current.orientation

    if (orientation == Configuration.ORIENTATION_PORTRAIT) {
        // Portrait Layout
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.fillMaxSize(),
        ) {
            Image(
                modifier = Modifier.padding(10.dp).size(250.dp),
                painter = painterResource(id = R.drawable.tasa_logo),
                contentDescription = "TASA logo",
            )
            Text(
                text = stringResource(R.string.welcome_to_tasa),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight(500),
                modifier = Modifier.padding(bottom = 30.dp),
                fontSize = 30.sp,
            )
            Button(
                onClick = onLoginRequested,
                modifier = Modifier.width(150.dp).padding(5.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            if (isSystemInDarkTheme()) {
                                Color.White
                            } else {
                                Color.Black
                            },
                    ),
            ) {
                Text(text = stringResource(R.string.log_in))
            }
            Button(
                onClick = onRegisterRequested,
                modifier = Modifier.width(150.dp).padding(5.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            if (isSystemInDarkTheme()) {
                                Color.White
                            } else {
                                Color.Black
                            },
                    ),
            ) {
                Text(text = stringResource(R.string.register))
            }
            val annotatedString =
                buildAnnotatedString {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(stringResource(R.string.continue_without_account))
                    }
                }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = annotatedString,
                    style = TextStyle(fontSize = 18.sp, color = MaterialTheme.colorScheme.primary),
                    modifier =
                        Modifier
                            .clickable { onContinueWithoutAccount() },
                )
            }
        }
    } else {
        // Landscape Layout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.fillMaxSize(),
        ) {
            Image(
                modifier = Modifier.size(170.dp),
                painter = painterResource(id = R.drawable.tasa_logo),
                contentDescription = "Tasa logo",
            )
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.welcome_to_tasa),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight(500),
                    fontSize = 17.sp,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = onLoginRequested,
                        modifier = Modifier.width(150.dp).height(40.dp).padding(2.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    if (isSystemInDarkTheme()) {
                                        Color.White
                                    } else {
                                        Color.Black
                                    },
                            ),
                    ) {
                        Text(text = stringResource(R.string.log_in))
                    }
                    Button(
                        onClick = onRegisterRequested,
                        modifier = Modifier.width(150.dp).height(40.dp).padding(2.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    if (isSystemInDarkTheme()) {
                                        Color.White
                                    } else {
                                        Color.Black
                                    },
                            ),
                    ) {
                        Text(text = stringResource(R.string.register))
                    }
                    val annotatedString =
                        buildAnnotatedString {
                            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                append(stringResource(R.string.continue_without_account))
                            }
                        }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = annotatedString,
                        style = TextStyle(fontSize = 18.sp, color = MaterialTheme.colorScheme.primary),
                        modifier =
                            Modifier
                                .clickable { onContinueWithoutAccount() },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    StartView(
        onLoginRequested = {},
        onRegisterRequested = {},
        onContinueWithoutAccount = {},
    )
}
