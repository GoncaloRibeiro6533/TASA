package com.tasa.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.domain.ApiError
import com.tasa.domain.UserInfoRepository
import com.tasa.repository.TasaRepo
import com.tasa.utils.Failure
import com.tasa.utils.Success
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

    data class EditingUsername(val profile: Profile) : ProfileScreenState

    data class Error(val error: ApiError) : ProfileScreenState
}

class ProfileScreenViewModel(
    private val userRepo: UserInfoRepository,
    private val repo: TasaRepo,
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
                        val userInfo = userRepo.getUserInfo() ?: throw Exception("User not authenticated")
                        ProfileScreenState.Success(Profile(userInfo.username, userInfo.email))
                    } catch (e: Throwable) {
                        ProfileScreenState.Error(ApiError("Error fetching user"))
                    }
            }
        } else {
            return null
        }
    }

    fun editUsername(newUsername: String) {
        if (_screenState.value != ProfileScreenState.Loading) {
            _screenState.value = ProfileScreenState.Loading
            viewModelScope.launch {
                _screenState.value =
                    try {
                        val user = repo.userRepo.changeUsername(newUsername)
                        when (user) {
                            is Success -> {
                                userRepo.updateUserInfo(user.value)
                                ProfileScreenState.Success(Profile(user.value.username, user.value.email))
                            }
                            is Failure -> ProfileScreenState.Error(user.value)
                        }
                    } catch (e: Throwable) {
                        ProfileScreenState.Error(ApiError("Error updating username"))
                    }
            }
        }
    }

    fun setEditState(profile: Profile) {
        if (_screenState.value != ProfileScreenState.EditingUsername(profile)) {
            _screenState.value = ProfileScreenState.EditingUsername(profile)
        }
    }

    fun setSuccessState(profile: Profile) {
        if (_screenState.value != ProfileScreenState.Success(profile)) {
            _screenState.value = ProfileScreenState.Success(profile)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class ProfileScreenViewModelFactory(
    private val repo: UserInfoRepository,
    private val db: TasaRepo,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileScreenViewModel(
            repo,
            db,
        ) as T
    }
}
