package com.tasa.ui.screens.about

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tasa.R

private const val EMAIL_SUBJECT = "About the Channel and Messages App"

class AboutActivity : ComponentActivity() {

    private fun onNavigationBack() {
        finish()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AboutScreen(
                onNavigateBack = { onNavigationBack() },
                onSendEmailRequested = { openSendEmail(it) },
                onOpenUrlRequested = { openURL(it) }
            )
        }
    }



    private fun openSendEmail(email: String) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                putExtra(Intent.EXTRA_SUBJECT, EMAIL_SUBJECT)
            }

            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast
                .makeText(
                    this,
                    R.string.activity_info_no_suitable_app,
                    Toast.LENGTH_LONG
                )
                .show()
        }
    }

    private fun openURL(url: Uri) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, url)
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast
                .makeText(
                    this,
                    R.string.activity_info_no_suitable_app,
                    Toast.LENGTH_LONG
                )
                .show()
        }
    }
}