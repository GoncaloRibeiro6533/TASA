package com.tasa.start

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.tasa.DependenciesContainer
import com.tasa.authentication.login.LoginActivity
import com.tasa.authentication.register.RegisterActivity
import com.tasa.homepage.HomePageActivity
import com.tasa.ui.theme.TasaTheme
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
            TasaTheme {
                StartScreen(
                    viewModel = viewModel,
                    onLoginRequested = { navigateTo(this, LoginActivity::class.java) },
                    onRegisterRequested = { navigateTo(this, RegisterActivity::class.java) },
                    onAboutRequested = { /* TODO: Navigate to About screen */ },
                    onLoggedIntent = { navigateTo(this, HomePageActivity::class.java) },
                )
            }
        }
    }
}
