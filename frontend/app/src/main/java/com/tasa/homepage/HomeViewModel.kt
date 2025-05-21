package com.tasa.homepage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.domain.ApiError
import com.tasa.domain.Rule
import com.tasa.domain.UserInfoRepository
import com.tasa.repository.TasaRepo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeScreenState {
    data object Uninitialized : HomeScreenState

    data object Loading : HomeScreenState

    data class Success(val rules: StateFlow<List<Rule>>) : HomeScreenState

    data class Error(val error: ApiError) : HomeScreenState
}

class HomePageScreenViewModel(
    private val repo: TasaRepo,
    private val userInfo: UserInfoRepository,
    initialState: HomeScreenState = HomeScreenState.Uninitialized,
) : ViewModel() {
    private val _state = MutableStateFlow<HomeScreenState>(initialState)
    val state: StateFlow<HomeScreenState> = _state.asStateFlow()

    private val _rules = MutableStateFlow<List<Rule>>(emptyList())
    val rules: StateFlow<List<Rule>> = _rules.asStateFlow()

    fun onFatalError() {
        viewModelScope.launch {
            try {
                repo.userRepo.clear()
                // TODO repo.ruleRepo.clear()
                repo.alarmRepo.clear()
                repo.eventRepo.clear()
                repo.locationRepo.clear()
                userInfo.clearUserInfo()
            } catch (e: Exception) {
                repo.userRepo.clear()
                // TODO repo.ruleRepo.clear()
                repo.alarmRepo.clear()
                repo.eventRepo.clear()
                repo.locationRepo.clear()
                userInfo.clearUserInfo()
            }
        }
    }

    fun loadLocalData(): Job? {
        if (_state.value is HomeScreenState.Loading) return null
        _state.value = HomeScreenState.Loading
        return viewModelScope.launch {
            try {
//                val user = userInfo.getUserInfo() ?: throw TasaException("User not found", null)
                repo.ruleRepo.fetchAllRules().collect { stream ->
                    _rules.value = stream
                    _state.value = HomeScreenState.Success(rules)
                }
            } catch (e: Throwable) {
                _state.value =
                    HomeScreenState.Error(ApiError("Error getting channels: ${e.message}"))
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory(
    private val userInfo: UserInfoRepository,
    private val repo: TasaRepo,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomePageScreenViewModel(
            repo = repo,
            userInfo = userInfo,
        ) as T
    }
}
