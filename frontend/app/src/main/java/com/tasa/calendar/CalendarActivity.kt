package com.tasa.calendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.tasa.DependenciesContainer
import com.tasa.authentication.login.LoginScreenViewModel
import com.tasa.authentication.login.LoginScreenViewModelFactory
import com.tasa.ui.screens.homepage.HomePageActivity
import com.tasa.utils.navigateTo
import kotlin.getValue

class CalendarActivity : ComponentActivity() {

    private val services by lazy { (application as DependenciesContainer).service }

    private val viewModel by viewModels<CalendarScreenViewModel>(
        factoryProducer = {
            CalendarScreenViewModelFactory(
                eventService = services.eventService,
            )
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CalendarScreen(
                    viewModel = viewModel,
                    onAddedEvent = {},
                    onNavigationBack = { navigateTo(this, HomePageActivity::class.java) },
                )
            }
        }
    }
}
