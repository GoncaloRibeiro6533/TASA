package com.tasa.authentication.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.domain.ApiError
import com.tasa.domain.user.User
import com.tasa.repository.TasaRepo
import com.tasa.service.TasaService
import com.tasa.service.UserService
import com.tasa.utils.Failure
import com.tasa.utils.Success
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RegisterScreenState {
    data object Idle : RegisterScreenState
    data object Loading : RegisterScreenState
    data class Success(val user: User) : RegisterScreenState
    data class Error(val error: ApiError): RegisterScreenState
}
class RegisterScreenViewModel(
    private val userServices: UserService,
    initialState: RegisterScreenState = RegisterScreenState.Idle
) : ViewModel() {

    private val _state = MutableStateFlow<RegisterScreenState>(initialState)
    val state = _state.asStateFlow()


    fun registerUser(username: String, password: String, email: String) {
        if (_state.value != RegisterScreenState.Loading) {
            _state.value = RegisterScreenState.Loading
            viewModelScope.launch {
                _state.value =
                    try {
                    val user = userServices.register(username, password, email)
                    when (user) {
                        is Success -> {
                            RegisterScreenState.Success(user.value)
                        }
                        is Failure -> RegisterScreenState.Error(user.value)
                    }
                } catch (e: Throwable) {
                    RegisterScreenState.Error(ApiError("Error registering user"))
                }
            }
        }
    }

    fun setIdleState() {
        _state.value = RegisterScreenState.Idle
    }
}

@Suppress("UNCHECKED_CAST")
class RegisterScreenViewModelFactory(private val userService: UserService): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RegisterScreenViewModel(userService) as T
    }
}