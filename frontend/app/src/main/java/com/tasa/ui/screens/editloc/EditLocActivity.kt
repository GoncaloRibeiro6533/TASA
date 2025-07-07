package com.tasa.ui.screens.editloc

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.tasa.DependenciesContainer
import com.tasa.domain.Location
import com.tasa.ui.screens.mylocations.MyLocationsActivity
import com.tasa.ui.theme.TasaTheme
import com.tasa.utils.navigateTo

class EditLocActivity : ComponentActivity() {
    private val repo by lazy {
        (application as DependenciesContainer).repo
    }

    private val viewModel by viewModels<EditLocScreenViewModel>(
        factoryProducer = {
            EditLocScreenViewModelFactory(
                repo = repo,
            )
        },
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val location = intent.getParcelableExtra("location", Location::class.java)
        if (location == null) {
            navigateTo(this, MyLocationsActivity::class.java)
            finish()
            return
        }

        setContent {
            TasaTheme {
                EditLocScreen(
                    viewModel = viewModel,
                    onNewCenter = {},
                    onNavigationBack = {
                        finish()
                    },
                    onUpdateSuccessful = {
                        navigateTo(this, MyLocationsActivity::class.java)
                    },
                    location = location,
                )
            }
        }
    }
}
