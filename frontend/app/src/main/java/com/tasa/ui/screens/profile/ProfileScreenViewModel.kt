package com.tasa.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.R
import com.tasa.domain.UserInfoRepository
import com.tasa.utils.StringResourceResolver
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Profile(
    val username: String,
    val email: String,
)

sealed interface ProfileScreenState {
    data object Idle : ProfileScreenState

    data object Loading : ProfileScreenState

    data class Success(val profile: Profile) : ProfileScreenState

    data class Error(val error: String) : ProfileScreenState
}

class ProfileScreenViewModel(
    private val userRepo: UserInfoRepository,
    private val stringResourceResolver: StringResourceResolver,
    initialState: ProfileScreenState = ProfileScreenState.Idle,
) : ViewModel() {
    private val _screenState = MutableStateFlow<ProfileScreenState>(initialState)
    val state: StateFlow<ProfileScreenState> = _screenState

    fun fetchProfile(): Job? {
        if (_screenState.value != ProfileScreenState.Loading) {
            _screenState.value = ProfileScreenState.Loading
            return viewModelScope.launch {
                _screenState.value =
                    try {
                        val userInfo = userRepo.getUserInfo()
                        if (userInfo == null) {
                            ProfileScreenState.Error(
                                stringResourceResolver.getString(R.string.user_to_found),
                            )
                            return@launch
                        }
                        ProfileScreenState.Success(Profile(userInfo.username, userInfo.email))
                    } catch (e: Throwable) {
                        ProfileScreenState.Error(
                            stringResourceResolver.getString(R.string.unexpected_error),
                        )
                    }
            }
        } else {
            return null
        }
    }
}

@Suppress("UNCHECKED_CAST")
class ProfileScreenViewModelFactory(
    private val repo: UserInfoRepository,
    private val stringResourceResolver: StringResourceResolver,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileScreenViewModel(
            repo,
            stringResourceResolver,
        ) as T
    }
}
