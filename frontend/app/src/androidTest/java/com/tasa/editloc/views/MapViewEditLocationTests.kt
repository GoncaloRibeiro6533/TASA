package com.tasa.editloc.views

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.tasa.domain.Location
import com.tasa.ui.screens.editloc.EDIT_LOC_CENTER_BUTTON
import com.tasa.ui.screens.editloc.EDIT_LOC_NAME_TEXT_FIELD
import com.tasa.ui.screens.editloc.EDIT_LOC_RADIUS_TEXT_FIELD
import com.tasa.ui.screens.editloc.EDIT_LOC_RULE_BUTTON
import com.tasa.ui.screens.editloc.EDIT_LOC_SAVE_BUTTON
import com.tasa.ui.screens.editloc.EDIT_LOC_VIEW
import com.tasa.ui.screens.editloc.EditLocView
import com.tasa.ui.screens.editloc.editview.EDIT_LOC_MAP_CANCEL_BUTTON
import com.tasa.ui.screens.editloc.editview.EDIT_LOC_MAP_NAME_TEXT_FIELD
import com.tasa.ui.screens.editloc.editview.EDIT_LOC_MAP_RADIUS_SLIDER
import com.tasa.ui.screens.editloc.editview.EDIT_LOC_MAP_SAVE_BUTTON
import com.tasa.ui.screens.editloc.editview.EDIT_LOC_MAP_VIEW
import com.tasa.ui.screens.editloc.editview.MapViewEditLocationView
import com.tasa.ui.screens.newLocation.TasaLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Rule
import org.junit.Test
import org.osmdroid.util.GeoPoint

class MapViewEditLocationTests {

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

    val locationName =
        MutableStateFlow<String>("TestLoc")

    val latitude = 0.0
    val longitude = 0.0

    val point =
        MutableStateFlow<GeoPoint>(
            GeoPoint(latitude, longitude)
        )

    val flowRadius =
        MutableStateFlow<Double>(30.0)

    @Test
    fun test_editLocMapView_displays_all_items() {
        composeTree.setContent {

            MapViewEditLocationView(
                previousLocation = location,
                selectedPoint = point,
                locationName = locationName,
                radius = flowRadius,
                onDismiss = {},
                onConfirm = {_, _, _, _, _ ->},
                onChangeRadius = {},
                onChangeLocationName = {},
            )
        }
        composeTree.onNodeWithTag(EDIT_LOC_MAP_VIEW).assertIsDisplayed()
        composeTree.onNodeWithTag(EDIT_LOC_MAP_NAME_TEXT_FIELD).assertIsDisplayed()
        composeTree.onNodeWithTag(EDIT_LOC_MAP_RADIUS_SLIDER).assertIsDisplayed()
        composeTree.onNodeWithTag(EDIT_LOC_MAP_CANCEL_BUTTON).assertIsDisplayed()
        composeTree.onNodeWithTag(EDIT_LOC_MAP_SAVE_BUTTON).assertIsDisplayed()
    }

    @Test
    fun test_editLocMapView_save_button_is_disabled_when_textFields_are_blank() {
        composeTree.setContent {

            val locationName =
                MutableStateFlow<String>("TestLoc")

            MapViewEditLocationView(
                previousLocation = location,
                selectedPoint = point,
                locationName = locationName,
                radius = flowRadius,
                onDismiss = {},
                onConfirm = {_, _, _, _, _ ->},
                onChangeRadius = {},
                onChangeLocationName = { locationName.value = it },
            )
        }
        composeTree.onNodeWithTag(EDIT_LOC_MAP_SAVE_BUTTON).assertIsDisplayed()
        composeTree.onNodeWithTag(EDIT_LOC_MAP_SAVE_BUTTON).assertIsEnabled()

        composeTree.onNodeWithTag(EDIT_LOC_MAP_NAME_TEXT_FIELD).performTextClearance()
        composeTree.waitForIdle()
        composeTree.onNodeWithTag(EDIT_LOC_MAP_SAVE_BUTTON).assertIsNotEnabled()

        composeTree.onNodeWithTag(EDIT_LOC_MAP_NAME_TEXT_FIELD).performTextInput(name)
        composeTree.waitForIdle()
        composeTree.onNodeWithTag(EDIT_LOC_MAP_SAVE_BUTTON).assertIsEnabled()

    }

    @Test
    fun when_cancelButton_clicked_then_onDismiss_action_is_called() {

        var submitted = false

        composeTree.setContent {

            MapViewEditLocationView(
                previousLocation = location,
                selectedPoint = point,
                locationName = locationName,
                radius = flowRadius,
                onDismiss = { -> submitted = true},
                onConfirm = {_, _, _, _, _ ->},
                onChangeRadius = {},
                onChangeLocationName = {},
            )
        }

        composeTree.onNodeWithTag(EDIT_LOC_MAP_CANCEL_BUTTON).performClick()
        assert(submitted)

    }

    @Test
    fun when_saveButton_clicked_then_onConfirm_action_is_called() {

        var submitted = false

        composeTree.setContent {

            MapViewEditLocationView(
                previousLocation = location,
                selectedPoint = point,
                locationName = locationName,
                radius = flowRadius,
                onDismiss = { },
                onConfirm = {_, _, _, _, _ -> submitted = true},
                onChangeRadius = {},
                onChangeLocationName = {},
            )
        }

        composeTree
            .onNodeWithTag(EDIT_LOC_MAP_SAVE_BUTTON)
            .performClick()
        assert(submitted)

    }

    @Test
    fun when_radius_changes_then_onChangeRadius_action_is_called() {

        var submitted = false

        composeTree.setContent {

            MapViewEditLocationView(
                previousLocation = location,
                selectedPoint = point,
                locationName = locationName,
                radius = flowRadius,
                onDismiss = { },
                onConfirm = {_, _, _, _, _ -> },
                onChangeRadius = {_ -> submitted = true},
                onChangeLocationName = {},
            )
        }

        composeTree
            .onNodeWithTag(EDIT_LOC_MAP_RADIUS_SLIDER)
            .performSemanticsAction(SemanticsActions.SetProgress) {it(45.0f)}
        composeTree
            .waitForIdle()
        assert(submitted)

    }

    @Test
    fun when_name_changes_then_onChangeLocationName_action_is_called() {

        var submitted = false

        composeTree.setContent {

            MapViewEditLocationView(
                previousLocation = location,
                selectedPoint = point,
                locationName = locationName,
                radius = flowRadius,
                onDismiss = { },
                onConfirm = {_, _, _, _, _ -> },
                onChangeRadius = {},
                onChangeLocationName = { _ -> submitted = true},
            )
        }

        composeTree
            .onNodeWithTag(EDIT_LOC_MAP_NAME_TEXT_FIELD)
            .performTextClearance()
        composeTree
            .waitForIdle()
        assert(submitted)

    }
}