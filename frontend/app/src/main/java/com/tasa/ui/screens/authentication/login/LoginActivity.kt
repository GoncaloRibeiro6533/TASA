package com.tasa.ui.screens.authentication.login

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.tasa.DependenciesContainer
import com.tasa.ui.screens.authentication.register.RegisterActivity
import com.tasa.ui.screens.homepage.HomePageActivity
import com.tasa.ui.screens.start.StartActivity
import com.tasa.ui.theme.TasaTheme
import com.tasa.utils.navigateTo

class LoginActivity : ComponentActivity() {
    private val userRepo by lazy { (application as DependenciesContainer).repo.userRepo }

    private val viewModel by viewModels<LoginScreenViewModel>(
        factoryProducer = {
            LoginScreenViewModelFactory(
                userRepository = userRepo,
            )
        },
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TasaTheme {
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = { navigateTo(this, HomePageActivity::class.java) },
                    onNavigationBack = { navigateTo(this, StartActivity::class.java) },
                    onRegisterRequested = { navigateTo(this, RegisterActivity::class.java) },
                )
            }
        }
    }
}
