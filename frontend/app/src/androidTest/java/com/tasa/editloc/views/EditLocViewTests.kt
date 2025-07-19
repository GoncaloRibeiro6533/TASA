package com.tasa.editloc.views

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.tasa.domain.Location
import com.tasa.ui.screens.editloc.EDIT_LOC_CENTER_BUTTON
import com.tasa.ui.screens.editloc.EDIT_LOC_NAME_TEXT_FIELD
import com.tasa.ui.screens.editloc.EDIT_LOC_RADIUS_TEXT_FIELD
import com.tasa.ui.screens.editloc.EDIT_LOC_VIEW
import com.tasa.ui.screens.editloc.EditLocView
import com.tasa.ui.screens.editloc.EDIT_LOC_RULE_BUTTON
import com.tasa.ui.screens.editloc.EDIT_LOC_SAVE_BUTTON
import org.junit.Rule
import org.junit.Test

class EditLocViewTests {
    @get:Rule
    val composeTree = createComposeRule()

    val name = "test"

    val radius = 30.0

    val location = Location(
        id = 1,
        name = name,
        latitude = 0.0,
        longitude = 0.0,
        radius = radius
    )

    @Test
    fun test_editLocView_displays_all_items() {
        composeTree.setContent {

            EditLocView(
                location = location,
                onSave = {_,_,_ ->},
                onNewCenter = {},
                onAddRule = {}
            )
        }
        composeTree.onNodeWithTag(EDIT_LOC_VIEW).assertIsDisplayed()
        composeTree.onNodeWithTag(EDIT_LOC_NAME_TEXT_FIELD).assertIsDisplayed()
        composeTree.onNodeWithTag(EDIT_LOC_RADIUS_TEXT_FIELD).assertIsDisplayed()
        composeTree.onNodeWithTag(EDIT_LOC_CENTER_BUTTON).assertIsDisplayed()
        composeTree.onNodeWithTag(EDIT_LOC_RULE_BUTTON).assertIsDisplayed()
        composeTree.onNodeWithTag(EDIT_LOC_SAVE_BUTTON).assertIsDisplayed()
    }

    @Test
    fun test_editLocView_save_button_is_disabled_when_textFields_are_blank() {
        composeTree.setContent {

            EditLocView(
                location = location,
                onSave = {_,_,_ ->},
                onNewCenter = {},
                onAddRule = {}
            )
        }
        composeTree.onNodeWithTag(EDIT_LOC_SAVE_BUTTON).assertIsDisplayed()
        composeTree.onNodeWithTag(EDIT_LOC_SAVE_BUTTON).assertIsEnabled()
        composeTree.onNodeWithTag(EDIT_LOC_NAME_TEXT_FIELD).performTextClearance()
        composeTree.onNodeWithTag(EDIT_LOC_SAVE_BUTTON).assertIsNotEnabled()
        composeTree.onNodeWithTag(EDIT_LOC_NAME_TEXT_FIELD).performTextInput(name)
        composeTree.onNodeWithTag(EDIT_LOC_SAVE_BUTTON).assertIsEnabled()
        composeTree.onNodeWithTag(EDIT_LOC_RADIUS_TEXT_FIELD).performTextClearance()
        composeTree.onNodeWithTag(EDIT_LOC_SAVE_BUTTON).assertIsNotEnabled()
        composeTree.onNodeWithTag(EDIT_LOC_RADIUS_TEXT_FIELD).performTextInput(radius.toString())
        composeTree.onNodeWithTag(EDIT_LOC_SAVE_BUTTON).assertIsEnabled()
    }

    @Test
    fun when_changeCenterButton_clicked_then_onNewCenter_action_is_called() {
        var submitted = false
        composeTree.setContent {

            EditLocView(
                location = location,
                onSave = {_,_,_ ->},
                onNewCenter = { -> submitted = true},
                onAddRule = {}
            )
        }

        composeTree.onNodeWithTag(EDIT_LOC_CENTER_BUTTON).performClick()
        assert(submitted)
    }

    @Test
    fun when_addRuleButton_clicked_then_onAddRule_action_is_called() {
        var submitted = false
        composeTree.setContent {

            EditLocView(
                location = location,
                onSave = {_,_,_ ->},
                onNewCenter = {},
                onAddRule = { -> submitted = true}
            )
        }

        composeTree.onNodeWithTag(EDIT_LOC_RULE_BUTTON).performClick()
        assert(submitted)
    }

    @Test
    fun when_saveButton_clicked_then_onSave_action_is_called() {
        var submitted = false
        composeTree.setContent {

            EditLocView(
                location = location,
                onSave = {_,_,_ -> submitted = true},
                onNewCenter = {},
                onAddRule = {}
            )
        }

        composeTree.onNodeWithTag(EDIT_LOC_SAVE_BUTTON).performClick()
        assert(submitted)
    }

}