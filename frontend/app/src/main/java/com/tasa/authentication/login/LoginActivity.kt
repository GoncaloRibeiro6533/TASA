package com.tasa.authentication.login

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tasa.authentication.register.RegisterActivity
import com.tasa.homepage.HomePageActivity
import com.tasa.service.mock.repo.UserRepoMock
import com.tasa.start.StartActivity
import com.tasa.ui.theme.TasaTheme
import com.tasa.utils.navigateTo

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LoginScreenViewModel(UserRepoMock()) as T
            }
        }

        setContent {
            TasaTheme {

                val viewModel: LoginScreenViewModel = viewModel(factory = viewModelFactory)
                LoginScreen(
                    viewModel = viewModel,
                    onLoginSuccess = { navigateTo(this, HomePageActivity::class.java) },
                    onNavigationBack = { navigateTo(this, StartActivity::class.java) },
                    onRegisterRequested = { navigateTo(this, RegisterActivity::class.java) }
                )
            }
        }
    }
}