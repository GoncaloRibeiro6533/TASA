package com.tasa.ui.screens.editloc

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import com.tasa.DependenciesContainer
import com.tasa.domain.Location
import com.tasa.ui.screens.mylocations.MyLocationsActivity
import com.tasa.ui.screens.newLocation.MapActivity
import com.tasa.ui.screens.start.StartActivity
import com.tasa.ui.theme.TasaTheme
import com.tasa.utils.navigateTo

class EditLocActivity : ComponentActivity() {
    private val repo by lazy {
        (application as DependenciesContainer).repo
    }
    private val stringResolver by lazy {
        (application as DependenciesContainer).stringResourceResolver
    }
    private val userInfoRepository by lazy {
        (application as DependenciesContainer).userInfoRepository
    }

    private val geofenceManager by lazy {
        (application as DependenciesContainer).geofenceManager
    }

    private val serviceKiller by lazy {
        (application as DependenciesContainer).serviceKiller
    }
    private val alarmScheduler by lazy {
        (application as DependenciesContainer).ruleScheduler
    }
    private val locationManager by lazy {
        (application as DependenciesContainer).locationUpdatesRepository
    }

    private val viewModel by viewModels<EditLocScreenViewModel>(
        factoryProducer = {
            EditLocScreenViewModelFactory(
                repo = repo,
                locationUpdatesRepository = locationManager,
                stringResolver = stringResolver,
                userInfo = userInfoRepository,
                geofenceManager = geofenceManager,
                serviceKiller = serviceKiller,
                alarmScheduler = alarmScheduler,
            )
        },
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
/*
        val location = intent.getParcelableExtra("location", Location::class.java)
        if (location == null) {
            navigateTo(this, MyLocationsActivity::class.java)
            finish()
            return
        }*/
        val location = intent.getParcelableExtra("location", Location::class.java)
        if (location == null) {
            navigateTo(this, MyLocationsActivity::class.java)
            finish()
            return
        }

        setContent {
            // Text("Editing: ${location?.name}")

            TasaTheme {
                EditLocScreen(
                    viewModel = viewModel,
                    onNewCenter = {
                            location ->
                        val intent =
                            Intent(this, MapActivity::class.java).apply {
                                putExtra("origin", "FromMyLocations")
                                putExtra("location", location)
                            }
                        startActivity(intent)
                    },
                    onNavigationBack = {
                        finish()
                    },
                    onUpdateSuccessful = {
                        navigateTo(this, MyLocationsActivity::class.java)
                    },
                    location = location,

                    onSessionExpired = {
                        finishAffinity()
                        navigateTo(
                            this@EditLocActivity,
                            StartActivity::class.java,
                        )
                    }
                )
            }
        }
    }
}
