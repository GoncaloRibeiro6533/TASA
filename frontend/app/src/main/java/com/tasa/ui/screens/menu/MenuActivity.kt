package com.tasa.ui.screens.menu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.work.WorkManager
import com.tasa.DependenciesContainer
import com.tasa.homepage.HomePageActivity
import com.tasa.start.StartActivity
import com.tasa.ui.screens.profile.ProfileActivity
import com.tasa.utils.navigateTo

class MenuActivity : ComponentActivity() {
    private val service by lazy { (application as DependenciesContainer).service }
    private val userInfoRepository by lazy { (application as DependenciesContainer).userInfoRepository }
    private val repo by lazy { (application as DependenciesContainer).repo }
    private val viewModel by viewModels<MenuViewModel>(
        factoryProducer = {
            MenuViewModelFactory(
                userInfoRepository,
                service,
                repo,
            )
        },
    )

    private val menuItems =
        listOf(
       /* MenuItem("About", "about screen", Icons.Default.Info) {
            navigateTo(
                this,
                AboutActivity::class.java
            )
        },*/
            MenuItem("Profile", "profile screen", Icons.Default.Person) {
                navigateTo(
                    this,
                    ProfileActivity::class.java,
                )
            },
            MenuItem("Logout", "logout screen", Icons.AutoMirrored.Filled.ExitToApp) {
                WorkManager.getInstance(applicationContext).cancelAllWork()
                viewModel.logout()
            },
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MenuScreen(
                viewModel = viewModel,
                menuItems = menuItems,
                onNavigateBack = {
                    navigateTo(this, HomePageActivity::class.java)
                    finish()
                },
                onLogout = {
                    WorkManager.getInstance(applicationContext).cancelAllWork()
                    finishAffinity()
                    navigateTo(this, StartActivity::class.java)
                },
            )
        }
    }
}
