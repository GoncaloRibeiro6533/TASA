package com.tasa.ui.screens.menu

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.res.stringResource
import androidx.work.WorkManager
import com.tasa.DependenciesContainer
import com.tasa.R
import com.tasa.ui.screens.homepage.HomePageActivity
import com.tasa.ui.screens.profile.ProfileActivity
import com.tasa.ui.screens.start.StartActivity
import com.tasa.utils.navigateTo
import kotlinx.parcelize.Parcelize

class MenuActivity : ComponentActivity() {
    private val userInfoRepository by lazy { (application as DependenciesContainer).userInfoRepository }
    private val repo by lazy { (application as DependenciesContainer).repo }
    private val serviceKiller by lazy { (application as DependenciesContainer).serviceKiller }
    private val locationUpdatesRepository by lazy {
        (application as DependenciesContainer).locationUpdatesRepository
    }
    private val alarmScheduler by lazy { (application as DependenciesContainer).ruleScheduler }
    private val geofenceManager by lazy { (application as DependenciesContainer).geofenceManager }
    private val viewModel by viewModels<MenuViewModel>(
        factoryProducer = {
            MenuViewModelFactory(
                userInfoRepository,
                repo,
                serviceKiller,
                locationUpdatesRepository,
                alarmScheduler,
                geofenceManager,
            )
        },
    )

    @Parcelize
    data class UserParcelable(
        val id: Int,
        val name: String,
        val email: String,
    ) : Parcelable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val isLocal =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getBooleanExtra("isLocal", false)
            } else {
                @Suppress("DEPRECATION")
                intent.getBooleanExtra("isLocal", false)
            }
        setContent {
            var menuItems =
                listOf(
                    MenuItem(stringResource(R.string.logout), "logout screen", Icons.AutoMirrored.Filled.ExitToApp) {
                        viewModel.logout()?.invokeOnCompletion {
                            WorkManager.getInstance(applicationContext).cancelAllWork()
                            finishAffinity()
                            navigateTo(this, StartActivity::class.java)
                        }
                    },
                )
            if (isLocal == false) {
                menuItems = listOf(
                    MenuItem(stringResource(R.string.profile), "profile screen", Icons.Default.Person) {
                        navigateTo(
                            this,
                            ProfileActivity::class.java,
                        )
                    },
                ) + menuItems
            }
            MenuScreen(
                viewModel = viewModel,
                menuItems = menuItems,
                onNavigateBack = {
                    navigateTo(this, HomePageActivity::class.java)
                    finish()
                },
                onLogoutIntent = {
                    WorkManager.getInstance(applicationContext).cancelAllWork()
                    finishAffinity()
                    navigateTo(this, StartActivity::class.java)
                },
            )
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navigateTo(this@MenuActivity, HomePageActivity::class.java)
                }
            },
        )
    }
}
