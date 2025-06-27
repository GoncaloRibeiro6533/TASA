package com.tasa.map

import android.Manifest
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.runner.RunWith

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
