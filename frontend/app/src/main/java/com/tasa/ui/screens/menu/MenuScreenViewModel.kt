package com.tasa.ui.screens.menu

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.domain.ApiError
import com.tasa.domain.UserInfoRepository
import com.tasa.location.LocationService
import com.tasa.repository.TasaRepo
import com.tasa.utils.Failure
import com.tasa.utils.ServiceKiller
import com.tasa.utils.Success
import kotlinx.coroutines.launch

sealed class MenuScreenState {
    data object Idle : MenuScreenState()

    data object LoggingOut : MenuScreenState()

    data object LoggedOut : MenuScreenState()

    data class Error(val error: ApiError) : MenuScreenState()
}

class MenuViewModel(
    private val userInfo: UserInfoRepository,
    private val repo: TasaRepo,
    private val serviceKiller: ServiceKiller,
    initialState: MenuScreenState = MenuScreenState.Idle,
) : ViewModel() {
    var state: MenuScreenState by mutableStateOf(initialState)
        private set

    fun logout() {
        if (state == MenuScreenState.LoggingOut) return
        state = MenuScreenState.LoggingOut
        viewModelScope.launch {
            state =
                try {
                    when (repo.userRepo.logout()) {
                        is Success -> {
                            repo.ruleRepo.clean()
                            repo.locationRepo.clear()
                            repo.alarmRepo.clear()
                            repo.eventRepo.clear()
                            repo.userRepo.clear()
                            userInfo.clearUserInfo()
                            serviceKiller.killServices(LocationService::class)
                            // Destroy the work manager
                            MenuScreenState.LoggedOut
                        }
                        is Failure -> {
                            repo.ruleRepo.clean()
                            repo.locationRepo.clear()
                            repo.alarmRepo.clear()
                            repo.eventRepo.clear()
                            repo.userRepo.clear()
                            userInfo.clearUserInfo()
                            MenuScreenState.LoggedOut
                        }
                    }
                } catch (e: Throwable) {
                    repo.ruleRepo.clean()
                    repo.locationRepo.clear()
                    repo.alarmRepo.clear()
                    repo.eventRepo.clear()
                    repo.userRepo.clear()
                    userInfo.clearUserInfo()
                    MenuScreenState.LoggedOut
                }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class MenuViewModelFactory(
    private val userInfo: UserInfoRepository,
    private val repo: TasaRepo,
    private val serviceKiller: ServiceKiller,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MenuViewModel(
            userInfo,
            repo,
            serviceKiller,
        ) as T
    }
}
