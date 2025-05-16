package com.tasa.authentication.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.domain.user.User
import com.tasa.service.mock.repo.UserRepoMock
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
    private val userRepo: UserRepoMock,
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

            val user =
                if (userRepo.findByEmail(email) != null) {
                    userRepo.findByEmail(email)
                } else {
                    userRepo.findUserByUsername(email).first()
                }
            if (user != null) {
                val authenticatedUser = userRepo.findUserByPassword(user.id, password)
                if (authenticatedUser != null) {
                    _state.value = LoginScreenState.Success(authenticatedUser)
                } else {
                    _state.value = LoginScreenState.Error("Wrong password")
                }
            } else {
                _state.value = LoginScreenState.Error("Email not found")
            }
        }
    }

    fun reset() {
        _state.value = LoginScreenState.Idle
    }
}

@Suppress("UNCHECKED_CAST")
class LoginScreenViewModelFactory(
    private val repo: UserRepoMock,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginScreenViewModel(
            repo,
        ) as T
    }
}
