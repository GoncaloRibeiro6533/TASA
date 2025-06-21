package com.tasa.ui.screens.homepage

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.alarm.AlarmScheduler
import com.tasa.domain.ApiError
import com.tasa.domain.Rule
import com.tasa.domain.RuleEvent
import com.tasa.domain.RuleLocation
import com.tasa.domain.UserInfoRepository
import com.tasa.domain.toTriggerTime
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
    private val alarmScheduler: AlarmScheduler,
    initialState: HomeScreenState = HomeScreenState.Uninitialized,
) : ViewModel() {
    private val _state = MutableStateFlow<HomeScreenState>(initialState)
    val state: StateFlow<HomeScreenState> = _state.asStateFlow()

    private val _rules = MutableStateFlow<List<Rule>>(emptyList())
    val rules: StateFlow<List<Rule>> = _rules.asStateFlow()

    /*
    fun onFatalError() {
        viewModelScope.launch {
            try {
                repo.userRepo.clear()
                repo.ruleRepo.clean()
                repo.alarmRepo.clear()
                repo.eventRepo.clear()
                repo.locationRepo.clear()
                userInfo.clearUserInfo()
            } catch (e: Exception) {
                repo.userRepo.clear()
                repo.ruleRepo.clean()
                repo.alarmRepo.clear()
                repo.eventRepo.clear()
                repo.locationRepo.clear()
                userInfo.clearUserInfo()
            }
        }
    }*/

    fun loadLocalData(): Job? {
        if (_state.value is HomeScreenState.Loading) return null
        _state.value = HomeScreenState.Loading
        return viewModelScope.launch {
            try {
                repo.ruleRepo.fetchAllRules().collect { stream ->
                    _rules.value = stream
                    _state.value = HomeScreenState.Success(rules)
                }
            } catch (e: Throwable) {
                _state.value =
                    HomeScreenState.Error(ApiError("Error getting rules"))
            }
        }
    }

    fun cancelRule(
        rule: Rule,
        context: Context,
    ) {
        viewModelScope.launch {
            try {
                when (rule) {
                    is RuleEvent -> {
                        repo.ruleRepo.deleteRuleEventByEventIdAndCalendarIdAndStarTimeAndEndtime(
                            rule.event.id,
                            rule.event.calendarId,
                            rule.startTime,
                            rule.endTime,
                        )
                        val alarmStart = repo.alarmRepo.getAlarmByTriggerTime(rule.startTime.toTriggerTime().value)
                        val alarmEnd = repo.alarmRepo.getAlarmByTriggerTime(rule.endTime.toTriggerTime().value)
                        if (alarmStart != null) {
                            repo.alarmRepo.deleteAlarm(alarmStart.id)
                            alarmScheduler.cancelAlarm(alarmStart.id, context)
                        }
                        if (alarmEnd != null) {
                            repo.alarmRepo.deleteAlarm(alarmEnd.id)
                            alarmScheduler.cancelAlarm(alarmEnd.id, context)
                        }
                    }
                    is RuleLocation -> repo.ruleRepo.deleteRuleLocationByName(rule.location.name)
                }
            } catch (e: Exception) {
                _state.value = HomeScreenState.Error(ApiError("Error cancelling rule"))
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory(
    private val userInfo: UserInfoRepository,
    private val repo: TasaRepo,
    private val alarmScheduler: AlarmScheduler,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomePageScreenViewModel(
            repo = repo,
            userInfo = userInfo,
            alarmScheduler = alarmScheduler,
        ) as T
    }
}
