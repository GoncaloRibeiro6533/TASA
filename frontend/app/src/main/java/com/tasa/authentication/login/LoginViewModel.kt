package com.tasa.authentication.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.domain.user.User
import com.tasa.service.UserService
import com.tasa.utils.Failure
import com.tasa.utils.Success
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LoginScreenState {
    object Idle : LoginScreenState

    object Loading : LoginScreenState

    data class Success(val user: User) : LoginScreenState

    data class Error(val message: String) : LoginScreenState
}

class LoginScreenViewModel(
    private val userService: UserService,
) : ViewModel() {
    private val _state = MutableStateFlow<LoginScreenState>(LoginScreenState.Idle)
    val state = _state.asStateFlow()

    fun login(
        email: String,
        password: String,
    ) {
        if (_state.value == LoginScreenState.Loading) return
        _state.value = LoginScreenState.Loading
        viewModelScope.launch {
            delay(1000)
            val authenticatedUser = userService.login(email, password)
            when (authenticatedUser) {
                is Success -> {
                    _state.value = LoginScreenState.Success(authenticatedUser.value.user)
                }
                is Failure -> {
                    _state.value = LoginScreenState.Error(authenticatedUser.value.message)
                }
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class LoginScreenViewModelFactory(
    private val userService: UserService,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginScreenViewModel(
            userService,
        ) as T
    }
}
