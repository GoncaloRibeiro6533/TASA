package com.tasa.ui.screens.start

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.tasa.DependenciesContainer
import com.tasa.ui.screens.about.AboutActivity
import com.tasa.ui.screens.authentication.login.LoginActivity
import com.tasa.ui.screens.authentication.register.RegisterActivity
import com.tasa.ui.screens.homepage.HomePageActivity
import com.tasa.utils.navigateTo

class StartActivity : ComponentActivity() {
    private val userInfoRepository by lazy { (application as DependenciesContainer).userInfoRepository }

    private val viewModel by viewModels<StartScreenViewModel>(
        factoryProducer = {
            StartScreenViewModelFactory(userInfoRepository)
        },
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.getSession()
        setContent {
            StartScreen(
                viewModel = viewModel,
                onLoginRequested = { navigateTo(this, LoginActivity::class.java) },
                onRegisterRequested = { navigateTo(this, RegisterActivity::class.java) },
                onAboutRequested = { navigateTo(this, AboutActivity::class.java) },
                onLoggedIntent = { navigateTo(this, HomePageActivity::class.java) },
                onContinueWithoutAccount = {
                    viewModel.setLocal()
                    navigateTo(this, HomePageActivity::class.java)
                },
            )
        }
    }
}
