package com.tasa.ui.screens.homepage

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.core.net.toUri
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.tasa.DependenciesContainer
import com.tasa.location.LocationService
import com.tasa.silence.LocationStateReceiver
import com.tasa.ui.components.PermissionBox
import com.tasa.ui.components.SpecialPermissionsHandler
import com.tasa.ui.screens.calendar.CalendarActivity
import com.tasa.ui.screens.menu.MenuActivity
import com.tasa.ui.screens.mylocations.MyLocationsActivity
import com.tasa.ui.screens.newLocation.MapActivity
import com.tasa.ui.screens.rule.EditRuleActivity
import com.tasa.ui.screens.start.StartActivity
import com.tasa.ui.theme.TasaTheme
import com.tasa.utils.navigateTo

class HomePageActivity : ComponentActivity() {
    private val userInfoRepository by lazy { (application as DependenciesContainer).userInfoRepository }

    private val repo by lazy { (application as DependenciesContainer).repo }

    private val alarmScheduler by lazy { (application as DependenciesContainer).ruleScheduler }

    private val geofenceManager by lazy {
        (application as DependenciesContainer).geofenceManager
    }

    private val serviceKiller by lazy {
        (applicationContext as DependenciesContainer).serviceKiller
    }

    private val stringResolver by lazy {
        (application as DependenciesContainer).stringResourceResolver
    }

    private val locationUpdatesRepository by lazy {
        (application as DependenciesContainer).locationUpdatesRepository
    }

    private val viewModel by viewModels<HomePageScreenViewModel>(
        factoryProducer = {
            HomeViewModelFactory(
                userInfoRepository,
                repo,
                alarmScheduler,
                geofenceManager,
                serviceKiller,
                stringResolver,
                locationUpdatesRepository,
            )
        },
    )

    private val locationStatusReceiver by lazy { LocationStateReceiver() }

    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ],
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.isLocal()
        if (!isLocationEnabled(this)) {
            /*val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_NO_HISTORY
            startActivity(intent)*/
        } else {
            requestGoogleLocationAccuracyPopup(this)
            if (!LocationService.isRunning) viewModel.registerGeofences()
        }
        registerReceiver(locationStatusReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
        val activityPermission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Manifest.permission.ACTIVITY_RECOGNITION
            } else {
                "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
            }
        val permissions =
            mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                activityPermission,
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR,
            )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions += Manifest.permission.POST_NOTIFICATIONS
        }
        setContent {
            TasaTheme {
                PermissionBox(
                    permissions = permissions,
                    requiredPermissions = permissions,
                    onSentToSettings = {
                        val intent =
                            Intent(ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = "package:$packageName".toUri()
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                    Intent.FLAG_ACTIVITY_NO_HISTORY
                            }
                        startActivity(intent)
                    },
                    onDenied = {
                        viewModel.clearOnFatalError().invokeOnCompletion {
                            navigateTo(
                                this@HomePageActivity,
                                StartActivity::class.java,
                            )
                            finish()
                        }
                    },
                    onGranted = @RequiresPermission(
                        allOf =
                            [
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACTIVITY_RECOGNITION,
                                Manifest.permission.ACCESS_NOTIFICATION_POLICY,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                                "com.google.android.gms.permission.ACTIVITY_RECOGNITION",
                                Manifest.permission.READ_CALENDAR,
                                Manifest.permission.POST_NOTIFICATIONS,
                                Manifest.permission.WRITE_CALENDAR,
                            ],
                    ) {
                        SpecialPermissionsHandler(
                            onRejected = {
                                viewModel.clearOnFatalError().invokeOnCompletion {
                                    navigateTo(
                                        this@HomePageActivity,
                                        StartActivity::class.java,
                                    )
                                    finish()
                                }
                            },
                        ) {
                            HomePageScreen(
                                viewModel = viewModel,
                                onNavigateToMyLocations = {
                                    navigateTo(
                                        this@HomePageActivity,
                                        MyLocationsActivity::class.java,
                                    )
                                },
                                onNavigateToCreateRuleEvent = {
                                    navigateTo(this@HomePageActivity, CalendarActivity::class.java)
                                },
                                onNavigationToMap = {
                                    navigateTo(this@HomePageActivity, MapActivity::class.java)
                                },
                                onNavigateToMyExceptions = {
                                    val intent =
                                        Intent(Settings.ACTION_ZEN_MODE_PRIORITY_SETTINGS).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                                Intent.FLAG_ACTIVITY_NO_HISTORY
                                        }
                                    startActivity(intent)
                                },
                                onMenuRequested = { isLocal: Boolean ->
                                    val intent =
                                        Intent(
                                            this@HomePageActivity,
                                            MenuActivity::class.java,
                                        ).putExtra("isLocal", isLocal)
                                    startActivity(intent)
                                },
                                onEditRule = { rule: EditRuleActivity.RuleParcelableEvent ->
                                    val intent =
                                        Intent(
                                            this@HomePageActivity,
                                            EditRuleActivity::class.java,
                                        ).putExtra("rule_event", rule)
                                    startActivity(intent)
                                },
                                onCancelRule = { rule ->
                                    viewModel.cancelRule(rule)
                                },
                                exitOnFatalError = {
                                    navigateTo(this@HomePageActivity, StartActivity::class.java)
                                    finish()
                                },
                            )
                        }
                    },
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(locationStatusReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver was not registered, ignore
        }
    }

    private fun isLocationEnabled(context: Context): Boolean {
        return try {
            val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                locationManager.isLocationEnabled
            } else {
                @Suppress("DEPRECATION")
                Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE) !=
                    Settings.Secure.LOCATION_MODE_OFF
            }
        } catch (e: Settings.SettingNotFoundException) {
            false
        }
    }

    @Suppress("DEPRECATION")
    private fun requestGoogleLocationAccuracyPopup(activity: Activity) {
        val locationRequest =
            LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }

        val builder =
            LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)

        val settingsClient = LocationServices.getSettingsClient(activity)

        settingsClient.checkLocationSettings(builder.build())
            .addOnSuccessListener {
            }
            .addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    try {
                        exception.startResolutionForResult(activity, 1234)
                    } catch (e: IntentSender.SendIntentException) {
                        e.printStackTrace()
                    }
                }
            }
    }
}
