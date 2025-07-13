package com.tasa.ui.screens.mylocations

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import com.tasa.DependenciesContainer
import com.tasa.ui.screens.editloc.EditLocActivity
import com.tasa.ui.screens.start.StartActivity
import com.tasa.ui.theme.TasaTheme
import com.tasa.utils.navigateTo
import org.osmdroid.util.GeoPoint

class MyLocationsActivity : ComponentActivity() {
    private val repo by lazy {
        (applicationContext as DependenciesContainer).repo
    }

    private val geofenceManager by lazy {
        (applicationContext as DependenciesContainer).geofenceManager
    }

    private val serviceKiller by lazy {
        (applicationContext as DependenciesContainer).serviceKiller
    }

    private val stringResolver by lazy {
        (applicationContext as DependenciesContainer).stringResourceResolver
    }

    private val userInfoRepository by lazy {
        (applicationContext as DependenciesContainer).userInfoRepository
    }

    private val alarmScheduler by lazy {
        (applicationContext as DependenciesContainer).ruleScheduler
    }

    private val locationUpdatesRepository by lazy {
        (applicationContext as DependenciesContainer).locationUpdatesRepository
    }
    private val viewModel by viewModels<MyLocationsScreenViewModel>(
        factoryProducer = {
            MyLocationsScreenViewModelFactory(
                repo = repo,
                geofenceManager = geofenceManager,
                serviceKiller = serviceKiller,
                stringResolver = stringResolver,
                userInfo = userInfoRepository,
                locationUpdatesRepository = locationUpdatesRepository,
                alarmScheduler = alarmScheduler,
            )
        },
    )

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadLocations()
        setContent {
            TasaTheme {
                MyLocationsScreen(
                    viewModel = viewModel,
                    onDeleteLocation = { location ->
                        viewModel.deleteLocation(location)
                    },
                    onEditLocation = { location ->
                        val intent =
                            Intent(this, EditLocActivity::class.java).apply {
                                putExtra("location", location)
                            }
                        val latitude = location.latitude
                        val longitude = location.longitude
                        println("locML lat:$latitude lon:$longitude")
                        startActivity(intent)
                    },
                    onNavigateBack = {
                        finish()
                    },
                    onCreateRuleLocationTimeless = { location ->
                        viewModel.createTimelessRuleLocation(location)
                    },
                    onSetCreateRuleState = {
                            location ->
                        viewModel.setCreatingRuleLocationState(location)
                    },
                    onSessionExpired = {
                        finishAffinity()
                        navigateTo(this, StartActivity::class.java)
                    },
                )
            }
        }
    }
}
