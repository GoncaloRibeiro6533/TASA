package com.tasa.ui.screens.start

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.domain.UserInfoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface StartScreenState {
    data object Logged : StartScreenState

    data object Idle : StartScreenState

    data object NotLogged : StartScreenState

    data object Saving : StartScreenState
}

class StartScreenViewModel(
    private val repo: UserInfoRepository,
    initialState: StartScreenState = StartScreenState.Idle,
) : ViewModel() {
    private val _state = MutableStateFlow<StartScreenState>(initialState)
    val state: StateFlow<StartScreenState> = _state.asStateFlow()

    fun getSession(): Job? {
        if (_state.value !is StartScreenState.Idle) return null
        return viewModelScope.launch {
            try {
                if (repo.isLocal()) {
                    _state.value = StartScreenState.Logged
                } else {
                    if (repo.getUserInfo() != null) {
                        _state.value = StartScreenState.Logged
                    } else {
                        _state.value = StartScreenState.NotLogged
                    }
                }
            } catch (e: Throwable) {
                _state.value = StartScreenState.NotLogged
            }
        }
    }

    fun setLocal(): Job? {
        if (_state.value is StartScreenState.Logged) return null
        return viewModelScope.launch {
            try {
                repo.setLocal(true)
                _state.value = StartScreenState.Saving
                val userInfo = repo.isLocal()
                Log.d("StartScreenViewModel", "setLocal: $userInfo")
            } catch (e: Throwable) {
                _state.value = StartScreenState.NotLogged
            }
        }
    }

    fun setLoggedState() {
        if (_state.value is StartScreenState.Logged) return
        _state.value = StartScreenState.Logged
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
