package com.tasa.About

import androidx.compose.material3.Scaffold
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tasa.ui.screens.about.ABOUT_VIEW
import com.tasa.ui.screens.about.AUTHOR_INFO
import com.tasa.ui.screens.about.AboutView
import com.tasa.ui.screens.about.EMAIL_BUTTON
import com.tasa.ui.screens.about.GITHUB_BUTTON
import com.tasa.ui.screens.homepage.EVENTS_BUTTON
import com.tasa.ui.screens.homepage.EXCEPTIONS_BUTTON
import com.tasa.ui.screens.homepage.HOME_VIEW
import com.tasa.ui.screens.homepage.HomePageView
import com.tasa.ui.screens.homepage.LOCATIONS_BUTTON
import com.tasa.ui.screens.homepage.MAP_BUTTON
import com.tasa.ui.screens.homepage.components.RULES_BAR
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class AboutViewTests {

    @get:Rule
    val composeTree = createComposeRule()


    @Test
    fun tesT_HomeView_displays_all_items() {
        composeTree.setContent {
            Scaffold { innerPadding ->

                AboutView(
                    innerPadding = innerPadding,
                    onOpenUrlRequested = {},
                    onSendEmailRequested = {}

                )

            }
        }
        composeTree.onNodeWithTag(ABOUT_VIEW).assertIsDisplayed()
        composeTree.onAllNodesWithTag(AUTHOR_INFO)[0].assertIsDisplayed()
        composeTree.onAllNodesWithTag(AUTHOR_INFO)[1].assertIsDisplayed()
        composeTree.onAllNodesWithTag(EMAIL_BUTTON)[0].assertIsDisplayed()
        composeTree.onAllNodesWithTag(EMAIL_BUTTON)[1].assertIsDisplayed()
        composeTree.onAllNodesWithTag(GITHUB_BUTTON)[0].assertIsDisplayed()
        composeTree.onAllNodesWithTag(GITHUB_BUTTON)[1].assertIsDisplayed()

    }

    @Test
    fun when_email_button_clicked_then_action_is_called() {

        var action = false
        composeTree.setContent {
            Scaffold { innerPadding ->

                AboutView(
                    innerPadding = innerPadding,
                    onOpenUrlRequested = {},
                    onSendEmailRequested = { _ -> action = true}

                )

            }
        }
        composeTree.onAllNodesWithTag(EMAIL_BUTTON)[0].performClick()
        assert(action)

    }

    @Test
    fun when_github_button_clicked_then_action_is_called() {

        var action = false
        composeTree.setContent {
            Scaffold { innerPadding ->

                AboutView(
                    innerPadding = innerPadding,
                    onOpenUrlRequested = { _ -> action = true},
                    onSendEmailRequested = {}

                )

            }
        }
        composeTree.onAllNodesWithTag(GITHUB_BUTTON)[0].performClick()
        assert(action)

    }
}