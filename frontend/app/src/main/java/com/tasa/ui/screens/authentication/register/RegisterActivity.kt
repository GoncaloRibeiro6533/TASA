package com.tasa.ui.screens.authentication.register

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.tasa.DependenciesContainer
import com.tasa.ui.screens.start.StartActivity
import com.tasa.ui.theme.TasaTheme
import com.tasa.utils.navigateTo
import kotlin.getValue

class RegisterActivity : ComponentActivity() {
    private val userRepo by lazy { (application as DependenciesContainer).repo.userRepo }

    private val viewModel by viewModels<RegisterScreenViewModel>(
        factoryProducer = {
            RegisterScreenViewModelFactory(
                userRepository = userRepo,
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
                    onRegisterSuccessful = {
                        finish()
                        navigateTo(this, StartActivity::class.java)
                    },
                    onNavigationBack = {
                        finish()
                        navigateTo(this, StartActivity::class.java)
                    },
                )
            }
        }
        onBackPressedDispatcher.addCallback(
            this,
            object : androidx.activity.OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                    navigateTo(this@RegisterActivity, StartActivity::class.java)
                }
            },
        )
    }
}
