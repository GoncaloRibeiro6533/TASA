package com.tasa.newevent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.tasa.DependenciesContainer
import com.tasa.calendar.CalendarActivity
import com.tasa.ui.screens.homepage.HomePageActivity
import com.tasa.ui.theme.TasaTheme
import com.tasa.utils.navigateTo
import kotlin.getValue

class NewEvenActivity : ComponentActivity() {

    private val service by lazy { (application as DependenciesContainer).service }

    private val viewModel by viewModels<NewEventViewModel>(
        factoryProducer = {
            NewEventScreenViewModelFactory(
                eventService = service.eventService,
            )
        },
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TasaTheme {
                NewEventScreen(
                    viewModel = viewModel,
                    onNavigationBack = { navigateTo(this, HomePageActivity::class.java) },
                    onAddedEvent = { navigateTo(this, CalendarActivity::class.java)}
                )
            }
        }
    }
}
