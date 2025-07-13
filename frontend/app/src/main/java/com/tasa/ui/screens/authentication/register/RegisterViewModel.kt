package com.tasa.ui.screens.authentication.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.domain.ApiError
import com.tasa.domain.user.User
import com.tasa.repository.interfaces.UserRepositoryInterface
import com.tasa.utils.Failure
import com.tasa.utils.Success
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RegisterScreenState {
    data object Idle : RegisterScreenState

    data object Loading : RegisterScreenState

    data class Success(val user: User) : RegisterScreenState

    data class Error(val error: ApiError) : RegisterScreenState
}

class RegisterScreenViewModel(
    private val userRepository: UserRepositoryInterface,
    initialState: RegisterScreenState = RegisterScreenState.Idle,
) : ViewModel() {
    private val _state = MutableStateFlow<RegisterScreenState>(initialState)
    val state = _state.asStateFlow()

    fun registerUser(
        username: String,
        password: String,
        email: String,
    ): Job? {
        if (_state.value == RegisterScreenState.Loading) return null
        _state.value = RegisterScreenState.Loading
        return viewModelScope.launch {
            _state.value =
                try {
                    val user = userRepository.createUser(username, email, password)
                    when (user) {
                        is Success -> {
                            val session = userRepository.createToken(username, password)
                            when (session) {
                                is Success -> {
                                    RegisterScreenState.Success(session.value)
                                }
                                is Failure -> RegisterScreenState.Error(session.value)
                            }
                        }
                        is Failure -> RegisterScreenState.Error(user.value)
                    }
                } catch (e: Throwable) {
                    RegisterScreenState.Error(ApiError("Error registering user"))
                }
        }
    }

    fun setIdleState() {
        _state.value = RegisterScreenState.Idle
    }
}

@Suppress("UNCHECKED_CAST")
class RegisterScreenViewModelFactory(private val userRepository: UserRepositoryInterface) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RegisterScreenViewModel(userRepository) as T
    }
}
