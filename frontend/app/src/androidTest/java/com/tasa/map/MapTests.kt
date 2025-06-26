package com.tasa.map



import android.Manifest
import androidx.activity.viewModels
import androidx.compose.material3.Scaffold
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tasa.DependenciesContainer

import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith



import kotlin.getValue

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.rule.GrantPermissionRule
import com.tasa.ui.screens.newLocation.MapView
import com.tasa.ui.screens.newLocation.MapViewRoot
import com.tasa.ui.screens.newLocation.MapsScreenState


@RunWith(AndroidJUnit4::class)
class MapViewTests {

    @get:Rule
    val composeTree = createComposeRule()

    @get:Rule
    val permissionRule: GrantPermissionRule =
        GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION)



/*
    @Test
    fun test_MapView_displays_all_items() {
        composeTree.setContent {
            MapView(
                onLocationSelected = {},
                onEditSearchBox = {},
                onCreateLocation = {},
                onSearch = {},
                onWriteSearchBox = {},
                onDismiss = {},
                onChangeLocationName = {},
                onChangeRadius = {},
                onConfirm = {_, _, _, _ ->},
                onTouchSearchBox = {},
                onUnTouchSearchBox = {},
            )


        }
        composeTree.onNodeWithTag(MAP_VIEW).assertIsDisplayed()
        //composeTree.onNodeWithTag(ADD_LOCATION_BUTTON).assertIsDisplayed()
        composeTree.onNodeWithTag(SEARCH_UI).assertIsDisplayed()
    }

 */


}

