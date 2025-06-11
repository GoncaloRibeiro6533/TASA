package com.tasa.mylocations

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tasa.DependenciesContainer
import com.tasa.mylocations.components.MyLocationsScreen
import com.tasa.ui.screens.homepage.HomePageActivity
import com.tasa.ui.theme.TasaTheme
import com.tasa.utils.navigateTo
import kotlin.jvm.java

class MyLocationsActivity : ComponentActivity() {
    private val repo by lazy {
        (applicationContext as DependenciesContainer).repo
    }

    private val viewModel by lazy {
        MyLocationsScreenViewModel(
            repo = repo,
            initialState = MyLocationsScreenState.Uninitialized,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadLocations()
        setContent {
            TasaTheme {
                MyLocationsScreen(
                    viewModel = viewModel,
                    onLocationSelected = {},
                    onAddLocation = {},
                    onDeleteLocation = {},
                    onEditLocation = {},
                    onNavigateBack = {
                        navigateTo(this, HomePageActivity::class.java)
                        finish()
                    },
                )
            }
        }
    }
}
