package com.tasa.authentication.login

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.tasa.DependenciesContainer
import com.tasa.authentication.register.RegisterActivity
import com.tasa.start.StartActivity
import com.tasa.ui.screens.homepage.HomePageActivity
import com.tasa.ui.theme.TasaTheme
import com.tasa.utils.navigateTo

class LoginActivity : ComponentActivity() {
    private val userService by lazy { (application as DependenciesContainer).service.userService }

    private val viewModel by viewModels<LoginScreenViewModel>(
        factoryProducer = {
            LoginScreenViewModelFactory(
                userService = userService,
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
