package com.tasa.homepage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.tasa.DependenciesContainer
import com.tasa.calendar.CalendarActivity
import com.tasa.newevent.NewEvenActivity
import com.tasa.newlocation.MapActivity
import com.tasa.ui.theme.TasaTheme
import com.tasa.utils.navigateTo

class HomePageActivity : ComponentActivity() {
    private val userInfoRepository by lazy { (application as DependenciesContainer).userInfoRepository }
    private val repo by lazy { (application as DependenciesContainer).repo }

    private val viewModel by viewModels<HomePageScreenViewModel>(
        factoryProducer = {
            HomeViewModelFactory(
                userInfoRepository,
                repo,
            )
        },
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadLocalData()
        setContent {
            TasaTheme {
                HomePageScreen(
                    viewModel = viewModel,
                    onNavigationToMap = { navigateTo(this, MapActivity::class.java) },
                    onNavigationToNewEvent = { navigateTo(this, NewEvenActivity::class.java) },
                    onNavigateToMyEvents = { navigateTo(this, CalendarActivity::class.java) },
                    onFatalError = { viewModel.onFatalError() },
                )
            }
        }
    }
}
