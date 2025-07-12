package com.tasa.start.views

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.tasa.ui.screens.start.CONTINUE_WITHOUT_ACCOUNT
import com.tasa.ui.screens.start.LOGIN_BUTTON
import com.tasa.ui.screens.start.REGISTER_BUTTON
import com.tasa.ui.screens.start.START_VIEW
import com.tasa.ui.screens.start.StartView
import org.junit.Rule
import org.junit.Test

class StartViewTests {
    @get:Rule
    val composeTree = createComposeRule()

    @Test
    fun when_Initialized_the_Start_view_is_shown() {
        composeTree.setContent {
            StartView(
                onLoginRequested = {},
                onRegisterRequested = {},
                onContinueWithoutAccount = {},
            )
        }
        composeTree.onNodeWithTag(START_VIEW).assertExists()
        composeTree.onNodeWithTag(LOGIN_BUTTON).assertIsDisplayed()
        composeTree.onNodeWithTag(REGISTER_BUTTON).assertIsDisplayed()
        composeTree.onNodeWithTag(CONTINUE_WITHOUT_ACCOUNT).assertIsDisplayed()
        composeTree.onNodeWithTag(START_VIEW).assertIsDisplayed()
    }

    @Test
    fun when_LoginButton_is_clicked_then_onLoginRequested_is_called() {
        var loginClicked = false
        composeTree.setContent {
            StartView(
                onLoginRequested = { loginClicked = true },
                onRegisterRequested = {},
                onContinueWithoutAccount = {},
            )
        }
        composeTree.onNodeWithTag(LOGIN_BUTTON).performClick()
        assert(loginClicked) { "Login button click did not trigger the expected action." }
    }

    @Test
    fun when_RegisterButton_is_clicked_then_onRegisterRequested_is_called() {
        var registerClicked = false
        composeTree.setContent {
            StartView(
                onLoginRequested = {},
                onRegisterRequested = { registerClicked = true },
                onContinueWithoutAccount = {},
            )
        }
        composeTree.onNodeWithTag(REGISTER_BUTTON).performClick()
        assert(registerClicked) { "Register button click did not trigger the expected action." }
    }

    @Test
    fun when_ContinueWithoutAccount_is_clicked_then_onContinueWithoutAccount_is_called() {
        var continueClicked = false
        composeTree.setContent {
            StartView(
                onLoginRequested = {},
                onRegisterRequested = {},
                onContinueWithoutAccount = { continueClicked = true },
            )
        }
        composeTree.onNodeWithTag(CONTINUE_WITHOUT_ACCOUNT).performClick()
        assert(continueClicked) { "Continue without account click did not trigger the expected action." }
    }

    @Test
    fun all_buttons_are_displayed() {
        composeTree.setContent {
            StartView(
                onLoginRequested = {},
                onRegisterRequested = {},
                onContinueWithoutAccount = {},
            )
        }
        composeTree.onNodeWithTag(LOGIN_BUTTON).assertIsDisplayed()
        composeTree.onNodeWithTag(REGISTER_BUTTON).assertIsDisplayed()
        composeTree.onNodeWithTag(CONTINUE_WITHOUT_ACCOUNT).assertIsDisplayed()
    }
}
