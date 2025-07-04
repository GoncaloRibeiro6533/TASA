package com.tasa.ui.screens.start

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.domain.UserInfoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

sealed interface StartScreenState {
    data object Idle : StartScreenState

    data object Logged : StartScreenState

    data object NotLogged : StartScreenState
}

class StartScreenViewModel(
    private val repo: UserInfoRepository,
    initialState: StartScreenState = StartScreenState.Idle,
) : ViewModel() {
    var state: StartScreenState by mutableStateOf<StartScreenState>(initialState)
        private set

    fun getSession(): Job? {
        return viewModelScope.launch {
            try {
                if (repo.isLocal()) {
                    state = StartScreenState.Logged
                    return@launch
                } else {
                    val userInfo = repo.getUserInfo()
                    state =
                        if (userInfo != null) {
                            StartScreenState.Logged
                        } else {
                            StartScreenState.NotLogged
                        }
                }
            } catch (e: Throwable) {
                state = StartScreenState.NotLogged
            }
        }
    }

    fun setLocal() {
        viewModelScope.launch {
            try {
                repo.setLocal(true)
            } catch (e: Throwable) {
                state = StartScreenState.NotLogged
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repo.clearUserInfo()
            state = StartScreenState.NotLogged
        }
    }
}

@Suppress("UNCHECKED_CAST")
class StartScreenViewModelFactory(
    private val repo: UserInfoRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StartScreenViewModel(repo) as T
    }
}
