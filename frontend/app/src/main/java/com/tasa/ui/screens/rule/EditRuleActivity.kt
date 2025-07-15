package com.tasa.ui.screens.rule

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import com.tasa.DependenciesContainer
import com.tasa.domain.Event
import com.tasa.domain.RuleEvent
import com.tasa.ui.screens.start.StartActivity
import com.tasa.utils.navigateTo
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

class EditRuleActivity : ComponentActivity() {
    private val repo by lazy { (application as DependenciesContainer).repo }

    private val ruleScheduler by lazy {
        (application as DependenciesContainer).ruleScheduler
    }

    private val queryCalendarService by lazy {
        (application as DependenciesContainer).queryCalendarService
    }

    private val stringResolver by lazy {
        (application as DependenciesContainer).stringResourceResolver
    }

    private val userInfoRepository by lazy {
        (application as DependenciesContainer).userInfoRepository
    }

    private val geofenceManager by lazy {
        (application as DependenciesContainer).geofenceManager
    }

    private val serviceKiller by lazy {
        (application as DependenciesContainer).serviceKiller
    }

    private val locationUpdatesRepository by lazy {
        (application as DependenciesContainer).locationUpdatesRepository
    }

    private lateinit var rule: RuleEvent

    private val viewModel by viewModels<EditRuleViewModel>(
        factoryProducer = {
            EditRuleViewModelFactory(
                repo = repo,
                alarmScheduler = ruleScheduler,
                rule = rule,
                queryCalendarService = queryCalendarService,
                stringResourceResolver = stringResolver,
                userInfo = userInfoRepository,
                serviceKiller = serviceKiller,
                geofenceManager = geofenceManager,
                locationUpdatesRepository = locationUpdatesRepository,
            )
        },
    )

    @Parcelize
    data class RuleParcelableLocation(
        val id: Int?,
        val startTime: LocalDateTime,
        val endTime: LocalDateTime,
        val locationId: Int?,
        val name: String,
        val latitude: Double,
        val longitude: Double,
        val radius: Double,
    ) : Parcelable

    @Parcelize
    data class RuleParcelableEvent(
        val id: Int,
        val startTime: LocalDateTime,
        val endTime: LocalDateTime,
        val eventTitle: String,
        val eventId: Long,
        val calendarId: Long,
    ) : Parcelable {
        fun toRuleEvent(): RuleEvent {
            return RuleEvent(
                id = id,
                startTime = startTime,
                endTime = endTime,
                event =
                    Event(
                        id = id,
                        eventId = eventId,
                        calendarId = calendarId,
                        title = eventTitle,
                    ),
            )
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ruleParcelableEvent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("rule_event", RuleParcelableEvent::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra("rule_event") as? RuleParcelableEvent
            }
        if (ruleParcelableEvent == null) {
            finish()
            return
        } else {
            rule = ruleParcelableEvent.toRuleEvent()
            viewModel.setEventTime(
                eventId = rule.event.eventId,
                calendarId = rule.event.calendarId,
            )
        }
        setContent {
            EditRuleScreen(
                viewModel = viewModel,
                onBackPressed = {
                    finish()
                },
                onRuleUpdate = {
                        startTime, endTime ->
                    viewModel.updateRule(
                        rule = rule,
                        newStartTime = startTime,
                        newEndTime = endTime,
                    )
                },
                onError = {
                    finish()
                },
                rule = rule,
                onSessionExpired = {
                    finishAffinity()
                    navigateTo(
                        this,
                        StartActivity::class.java,
                    )
                },
            )
        }
    }
}
