package com.tasa.homepage

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.tasa.DependenciesContainer
import com.tasa.calendar.CalendarActivity
import com.tasa.newlocation.MapActivity
import com.tasa.ui.screens.menu.MenuActivity
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
                   /* onNavigationToMap = { navigateTo(this, MapActivity::class.java) },
                    onNavigationToNewEvent = { navigateTo(this, NewEvenActivity::class.java) },
                    onNavigateToMyEvents = { navigateTo(this, CalendarActivity::class.java) },*/
                    onNavigateToCreateRuleEvent = { navigateTo(this, CalendarActivity::class.java) },
                    onNavigationToMap = { startActivity(Intent(this, MapActivity::class.java)) },
                    onNavigateToMyExceptions = {
                        val intent =
                            Intent(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_NO_HISTORY
                            }
                        startActivity(intent)
                    },
                    onMenuRequested = {
                        navigateTo(this, MenuActivity::class.java)
                        finish()
                    },
                    onFatalError = { viewModel.onFatalError() },
                )
            }
        }
    }
}
