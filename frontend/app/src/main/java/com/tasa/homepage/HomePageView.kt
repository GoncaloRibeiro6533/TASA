package com.tasa.homepage

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomePageView(
    onNavigationToMap: () -> Unit,
    onNavigationToNewEvent: () -> Unit,
    onNavigationToMyEvents: () -> Unit,
) {
    HomePageLayout(onNavigationToMap, onNavigationToNewEvent, onNavigationToMyEvents)
}

@Composable
fun TextBox(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun HomePageLayout(
    onNavigationToMap: () -> Unit,
    onNavigationToNewEvent: () -> Unit,
    onNavigationToMyEvents: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
    ) {
        val gray = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(300.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "TASA",
                fontSize = 40.sp,
                textAlign = TextAlign.Center,
            )
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            val aspectRatio = 1f // Makes it square
            Button(
                onClick = {},
                modifier =
                    Modifier
                        .weight(1f)
                        .aspectRatio(aspectRatio)
                        .padding(4.dp),
                colors = gray,
            ) {
                TextBox("MY LOCATIONS")
            }
            Button(
                onClick = { onNavigationToMyEvents() },
                modifier =
                    Modifier
                        .weight(1f)
                        .aspectRatio(aspectRatio)
                        .padding(4.dp),
                colors = gray,
            ) {
                TextBox("MY EVENTS")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Row - 3 smaller square buttons
        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { onNavigationToMap() },
                modifier =
                    Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                colors = gray,
            ) {
                TextBox("ADD NEW LOCATION")
            }
            Button(
                onClick = {},
                modifier =
                    Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                colors = gray,
            ) {
                TextBox("MY EXCEPTIONS")
            }
            Button(
                onClick = { onNavigationToNewEvent() },
                modifier =
                    Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(4.dp),
                colors = gray,
            ) {
                TextBox("ADD NEW EVENT")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    HomePageView({}, {}, {})
}
