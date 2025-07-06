package com.tasa.ui.screens.calendar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tasa.DependenciesContainer
import com.tasa.ui.theme.TasaTheme

class CalendarActivity : ComponentActivity() {
    private val ruleScheduler by lazy {
        (application as DependenciesContainer).ruleScheduler
    }

    private val repo by lazy {
        (application as DependenciesContainer).repo
    }

    private val queryCalendarService by lazy {
        (application as DependenciesContainer).queryCalendarService
    }

    private val stringResolver by lazy {
        (application as DependenciesContainer).stringResourceResolver
    }

    private val viewModel by viewModels<CalendarScreenViewModel> {
        CalendarViewModelFactory(
            ruleScheduler = ruleScheduler,
            repo = repo,
            queryCalendarService = queryCalendarService,
            stringResolver = stringResolver,
        )
    }

    fun checkAndRequestCalendarPermission(activity: ComponentActivity) {
        val permission = Manifest.permission.READ_CALENDAR
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), 1)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestCalendarPermission(this)
        // tODO job is being cancelled when activity is recreated
        viewModel.loadEvents()
        setContent {
            TasaTheme {
                /*PermissionBox(
                    permission = Manifest.permission.READ_CALENDAR,
                ) {*/
                CalendarScreen(
                    onEventSelected = { calendarEvent ->
                        viewModel.onEventSelected(calendarEvent)
                    },
                    onNavigationBack = {
                        finish()
                    },
                    onCancel = { viewModel.onCancel() },
                    onCreateRuleEvent = { event, startTime, endTime ->
                        viewModel.onCreateRuleEvent(
                            event,
                            startTime,
                            endTime,
                        )
                    },
                    viewModel = viewModel,
                    onDateSelected = { day ->
                        viewModel.onDaySelected(day)
                    },
                )
            }
        }
        // }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        finish()
        super.onBackPressedDispatcher
    }
}
