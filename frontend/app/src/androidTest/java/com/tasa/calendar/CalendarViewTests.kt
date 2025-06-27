package com.tasa.calendar

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tasa.domain.CalendarEvent
import com.tasa.ui.screens.calendar.CALENDAR_VIEW
import com.tasa.ui.screens.calendar.CalendarView
import com.tasa.ui.screens.calendar.components.DATE_BAR
import com.tasa.ui.screens.calendar.components.EVENT_CARD
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class CalendarViewTests {
    @get:Rule
    val composeTree = createComposeRule()

    val sampleEvents =
        listOf(
            CalendarEvent(
                eventId = 1L,
                calendarId = 1L,
                title = "Evento 1",
                startTime = LocalDateTime.now(),
                endTime = LocalDateTime.now().plusHours(1),
            ),
            CalendarEvent(
                eventId = 2L,
                calendarId = 1L,
                title = "Evento 2",
                startTime = LocalDateTime.now().plusDays(1),
                endTime = LocalDateTime.now().plusDays(1).plusHours(2),
            ),
        )

    @Test
    fun test_CalendarView_displays_all_items() {
        composeTree.setContent {
            CalendarView(
                onEventSelected = {},
                eventsFlow = MutableStateFlow(emptyList()),
            )
        }
        composeTree.onNodeWithTag(CALENDAR_VIEW).assertIsDisplayed()
        composeTree.onNodeWithTag(EVENT_CARD).assertIsNotDisplayed()
        composeTree.onNodeWithTag(DATE_BAR).assertIsDisplayed()
    }

    @Test
    fun test_CalendarView_with_events_displays_all_items() {
        composeTree.setContent {
            CalendarView(
                onEventSelected = {},
                eventsFlow = MutableStateFlow(sampleEvents),
            )
        }
        composeTree.onNodeWithTag(CALENDAR_VIEW).assertIsDisplayed()
        composeTree.onNodeWithTag(EVENT_CARD).assertIsDisplayed()
        composeTree.onNodeWithTag(DATE_BAR).assertIsDisplayed()
    }
}
