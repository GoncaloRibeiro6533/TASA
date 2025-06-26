package com.tasa.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


import com.tasa.ui.screens.homepage.EVENTS_BUTTON
import com.tasa.ui.screens.homepage.EXCEPTIONS_BUTTON
import com.tasa.ui.screens.homepage.HOME_VIEW
import com.tasa.ui.screens.homepage.HomePageView
import com.tasa.ui.screens.homepage.LOCATIONS_BUTTON
import com.tasa.ui.screens.homepage.MAP_BUTTON
import com.tasa.ui.screens.homepage.components.RULES_BAR
import com.tasa.ui.screens.newLocation.MapView
import kotlinx.coroutines.flow.MutableStateFlow

@RunWith(AndroidJUnit4::class)
class HomeViewTests {

    @get:Rule
    val composeTree = createComposeRule()


    @Test
    fun testHomeView_displays_all_items() {
        composeTree.setContent {
            HomePageView(
                rules = MutableStateFlow(emptyList()),
                onEdit = {},
                onDelete = {},
                onNavigationToMap = {},
                onNavigateToMyLocations = {},
                onNavigationToMyExceptions = {},
                onNavigateToCreateRuleEvent = {}
            )
        }
        composeTree.onNodeWithTag(HOME_VIEW).assertIsDisplayed()
        composeTree.onNodeWithTag(LOCATIONS_BUTTON).assertIsDisplayed()
        composeTree.onNodeWithTag(EVENTS_BUTTON).assertIsDisplayed()
        composeTree.onNodeWithTag(EXCEPTIONS_BUTTON).assertIsDisplayed()
        composeTree.onNodeWithTag(MAP_BUTTON).assertIsDisplayed()
        composeTree.onNodeWithTag(RULES_BAR).assertIsDisplayed()
    }

    @Test
    fun when_locations_button_clicked_then_navigation_action_is_called() {
        var navigated = false
        composeTree.setContent {
            HomePageView(
                rules = MutableStateFlow(emptyList()),
                onEdit = {},
                onDelete = {},
                onNavigationToMap = {},
                onNavigateToMyLocations = { -> navigated = true},
                onNavigationToMyExceptions = {},
                onNavigateToCreateRuleEvent = {}
            )
        }

        composeTree.onNodeWithTag(LOCATIONS_BUTTON).performClick()
        assert(navigated)
    }

    @Test
    fun when_events_button_clicked_then_navigation_action_is_called() {
        var navigated = false
        composeTree.setContent {
            HomePageView(
                rules = MutableStateFlow(emptyList()),
                onEdit = {},
                onDelete = {},
                onNavigationToMap = {},
                onNavigateToMyLocations = {},
                onNavigationToMyExceptions = {},
                onNavigateToCreateRuleEvent = { -> navigated = true}
            )
        }

        composeTree.onNodeWithTag(EVENTS_BUTTON).performClick()
        assert(navigated)
    }

    @Test
    fun when_exceptions_button_clicked_then_navigation_action_is_called() {
        var navigated = false
        composeTree.setContent {
            HomePageView(
                rules = MutableStateFlow(emptyList()),
                onEdit = {},
                onDelete = {},
                onNavigationToMap = {},
                onNavigateToMyLocations = {},
                onNavigationToMyExceptions = { -> navigated = true},
                onNavigateToCreateRuleEvent = {}
            )
        }

        composeTree.onNodeWithTag(EXCEPTIONS_BUTTON).performClick()
        assert(navigated)
    }

    @Test
    fun when_map_button_clicked_then_navigation_action_is_called() {
        var navigated = false
        composeTree.setContent {
            HomePageView(
                rules = MutableStateFlow(emptyList()),
                onEdit = {},
                onDelete = {},
                onNavigationToMap = { -> navigated = true},
                onNavigateToMyLocations = {},
                onNavigationToMyExceptions = {},
                onNavigateToCreateRuleEvent = {}
            )
        }

        composeTree.onNodeWithTag(MAP_BUTTON).performClick()
        assert(navigated)
    }
}
