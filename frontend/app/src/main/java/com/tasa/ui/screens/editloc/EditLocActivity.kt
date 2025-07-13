package com.tasa.ui.screens.editloc

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.tasa.DependenciesContainer
import com.tasa.domain.Location
import com.tasa.ui.screens.mylocations.MyLocationsActivity
import com.tasa.ui.screens.newLocation.MapActivity
import com.tasa.ui.screens.start.StartActivity
import com.tasa.ui.theme.TasaTheme
import com.tasa.utils.navigateTo
import org.osmdroid.util.GeoPoint

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
    private lateinit var point: GeoPoint



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
                initialPoint = point
            )
        },
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val location = intent.getParcelableExtra("location", Location::class.java)
        if (location == null) {
            navigateTo(this, MyLocationsActivity::class.java)
            finish()
            return
        }

        point = GeoPoint(location.latitude, location.longitude)

        val latitude = location.latitude
        val longitude = location.longitude
        println("locAt lat:$latitude lon:$longitude")


        setContent {
            // Text("Editing: ${location?.name}")

            TasaTheme {
                EditLocScreen(
                    viewModel = viewModel,

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
                    },
                    onUpdateLocationName = { name ->
                        viewModel.editLocationName(name)
                    },
                    onUpdateRadius = { it ->
                        viewModel.updateRadius(it)
                    },
                    onEditCenterButton = { _, name, radius, latitude, longitude ->

                        println("locactfun lat:$latitude lon:$longitude")
                        viewModel.onChangeCenter(
                            location = location,
                            locationName = name,
                            radius = radius,
                            latitude = latitude,
                            longitude = longitude,
                        )
                    },
                    onDismissChangingCenter = {
                        finish()
                    },
                    onLocationSelected = { geoPoint ->
                        viewModel.updateSelectedPoint(geoPoint)
                    },
                    onAddRule = {
                        viewModel.createTimelessRule(location)
                    }

                )
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getLocation(): Location? {
        val location = intent.getParcelableExtra("location", Location::class.java)
        return location
    }


}



