package com.tasa.ui.screens.calendar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tasa.DependenciesContainer
import com.tasa.ui.screens.homepage.HomePageActivity
import com.tasa.utils.navigateTo

class CalendarActivity : ComponentActivity() {
    private val ruleScheduler by lazy {
        (application as DependenciesContainer).ruleScheduler
    }

    private val repo by lazy {
        (application as DependenciesContainer).repo
    }
    private val viewModel by viewModels<CalendarScreenViewModel>(
        factoryProducer = { CalendarViewModelFactory(ruleScheduler, repo) },
    )

    fun checkAndRequestCalendarPermission(activity: ComponentActivity) {
        val permission = Manifest.permission.READ_CALENDAR
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), 1)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestCalendarPermission(this)
        viewModel.loadEvents(this)
        setContent {
            MaterialTheme {
                CalendarScreen(
                    onEventSelected = { calendarEvent ->
                        viewModel.onEventSelected(calendarEvent)
                    },
                    onNavigationBack = {
                        navigateTo(this, HomePageActivity::class.java)
                        finish()
                    },
                    onCancel = { viewModel.onCancel() },
                    onCreateRuleEvent = { event, startTime, endTime ->
                        viewModel.onCreateRuleEvent(
                            event,
                            startTime,
                            endTime,
                            this,
                        )
                        navigateTo(this, HomePageActivity::class.java)
                        finish()
                    },
                    viewModel = viewModel,
                )
            }
        }
    }
}
