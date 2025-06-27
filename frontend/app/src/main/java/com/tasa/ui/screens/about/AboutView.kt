package com.tasa.ui.screens.about

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasa.R

/**
 * Tags used to identify the components of the AboutScreen in automated tests
 */
const val ABOUT_VIEW = "AboutScreenTestTag"
const val AUTHOR_INFO = "AuthorInfoTestTag"
const val EMAIL_BUTTON = "SocialsElementTestTag"
const val GITHUB_BUTTON = "NavigateBack"

@Composable
fun AboutView(
    innerPadding: PaddingValues,
    onSendEmailRequested: (String) -> Unit,
    onOpenUrlRequested: (Uri) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .testTag(ABOUT_VIEW),
    ) {
        Text(text = "About", fontSize = 30.sp, fontWeight = FontWeight.Bold)
        defaultAuthors.forEach { author ->
            Author(author, onSendEmailRequested, onOpenUrlRequested)
        }
    }
}

/**
 * Composable used to display information about the author of the application
 */
@Composable
private fun Author(
    author: CreatorInfo,
    onSendEmailRequested: (String) -> Unit = { },
    onOpenUrlRequested: (Uri) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .padding(16.dp)
                .testTag(AUTHOR_INFO),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .weight(1f)
                    .clickable { onSendEmailRequested(author.email) }
                    .testTag(EMAIL_BUTTON),
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_user_img),
                contentDescription = null,
                modifier = Modifier.sizeIn(50.dp, 50.dp, 100.dp, 100.dp),
            )
            Text(text = author.name, style = MaterialTheme.typography.titleLarge)
            Icon(imageVector = Icons.Default.Email, contentDescription = null)
        }
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            author.socials.forEach {
                Social(id = it.imageId, onClick = { onOpenUrlRequested(it.link) })
            }
        }
    }
}

@Composable
private fun Social(
    @DrawableRes id: Int,
    onClick: () -> Unit,
) {
    Image(
        painter = painterResource(id = id),
        contentDescription = null,
        modifier =
            Modifier
                .sizeIn(maxWidth = 64.dp)
                .testTag(GITHUB_BUTTON)
                .clickable { onClick() },
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun InfoScreenPreview() {
    AboutScreen()
}
