package com.tasa.ui.screens.homepage

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tasa.DependenciesContainer
import com.tasa.domain.Rule
import com.tasa.ui.components.PermissionBox
import com.tasa.ui.screens.calendar.CalendarActivity
import com.tasa.ui.screens.menu.MenuActivity
import com.tasa.ui.screens.mylocations.MyLocationsActivity
import com.tasa.ui.screens.newLocation.MapActivity
import com.tasa.ui.screens.rule.EditRuleActivity
import com.tasa.ui.theme.TasaTheme
import com.tasa.utils.navigateTo

class HomePageActivity : ComponentActivity() {
    private val userInfoRepository by lazy { (application as DependenciesContainer).userInfoRepository }

    private val repo by lazy { (application as DependenciesContainer).repo }

    private val alarmScheduler by lazy { (application as DependenciesContainer).ruleScheduler }

    private val viewModel by viewModels<HomePageScreenViewModel>(
        factoryProducer = {
            HomeViewModelFactory(
                userInfoRepository,
                repo,
                alarmScheduler,
            )
        },
    )

    fun checkAndRequestNotificationPolicyPermission(activity: ComponentActivity) {
        val permission = Manifest.permission.ACCESS_NOTIFICATION_POLICY
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(permission), 1)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // registerReceiver(LocationStateReceiver(), IntentFilter(android.location.LocationManager.PROVIDERS_CHANGED_ACTION))
        viewModel.loadLocalData()
        val activityPermission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Manifest.permission.ACTIVITY_RECOGNITION
            } else {
                "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
            }
        val permissions =
            mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                activityPermission,
                Manifest.permission.READ_CALENDAR,
            )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions += Manifest.permission.POST_NOTIFICATIONS
        }
        setContent {
            TasaTheme {
                PermissionBox(
                    permissions = permissions,
                    requiredPermissions = permissions,
                    onGranted = @RequiresPermission(
                        allOf =
                            [
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACTIVITY_RECOGNITION,
                                "com.google.android.gms.permission.ACTIVITY_RECOGNITION",
                                Manifest.permission.READ_CALENDAR,
                                Manifest.permission.POST_NOTIFICATIONS,
                            ],
                    ) {
                        HomePageScreen(
                            viewModel = viewModel,
                            onNavigateToMyLocations = {
                                navigateTo(this@HomePageActivity, MyLocationsActivity::class.java)
                            },
                            onNavigateToCreateRuleEvent = {
                                navigateTo(this@HomePageActivity, CalendarActivity::class.java)
                            },
                            onNavigationToMap = {
                                startActivity(Intent(this@HomePageActivity, MapActivity::class.java))
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
                            onMenuRequested = {
                                navigateTo(this@HomePageActivity, MenuActivity::class.java)
                                finish()
                            },
                            onFatalError = { finish() },
                            // TODO close app
                            onEditRule = { rule: EditRuleActivity.RuleParcelableEvent ->
                                val intent = Intent(this@HomePageActivity, EditRuleActivity::class.java).putExtra("rule_event", rule)
                                startActivity(intent)
                                finish()
                            },
                            onCancelRule = {
                                    rule: Rule ->
                                viewModel.cancelRule(rule, this@HomePageActivity)
                            },
                        )
                    },
                )
            }
        }
    }

    /*fun hasLocationPermissions(): Boolean {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val background =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }

        return fine && background
    }*/
}
