package com.tasa.ui.screens.authentication.register

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.tasa.DependenciesContainer
import com.tasa.ui.screens.authentication.login.LoginActivity
import com.tasa.ui.screens.start.StartActivity
import com.tasa.ui.theme.TasaTheme
import com.tasa.utils.navigateTo
import kotlin.getValue

class RegisterActivity : ComponentActivity() {
    private val userService by lazy { (application as DependenciesContainer).service.userService }

    private val viewModel by viewModels<RegisterScreenViewModel>(
        factoryProducer = {
            RegisterScreenViewModelFactory(
                userService = userService,
            )
        },
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TasaTheme {
                RegisterScreen(
                    viewModel = viewModel,
                    onSubmit = { email, username, password ->
                        viewModel.registerUser(email, username, password)
                    },
                    onRegisterSuccessful = { navigateTo(this, LoginActivity::class.java) },
                    onNavigationBack = { navigateTo(this, StartActivity::class.java) },
                )
            }
        }
    }
}
