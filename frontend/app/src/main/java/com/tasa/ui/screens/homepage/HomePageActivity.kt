package com.tasa.ui.screens.homepage

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tasa.DependenciesContainer
import com.tasa.domain.Rule
import com.tasa.mylocations.MyLocationsActivity
import com.tasa.newlocation.MapActivity
import com.tasa.ui.screens.calendar.CalendarActivity
import com.tasa.ui.screens.menu.MenuActivity
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
        val notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            this.startActivity(intent)
        }
        viewModel.loadLocalData()
        setContent {
            TasaTheme {
                HomePageScreen(
                    viewModel = viewModel,
                    onNavigateToMyLocations = {
                        navigateTo(this, MyLocationsActivity::class.java)
                        finish()
                    },
                    onNavigateToCreateRuleEvent = {
                        navigateTo(this, CalendarActivity::class.java)
                    },
                    onNavigationToMap = {
                        startActivity(Intent(this, MapActivity::class.java))
                        finish()
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
                        navigateTo(this, MenuActivity::class.java)
                        finish()
                    },
                    onFatalError = { finish() },
                    // TODO close app
                    onEditRule = { rule: EditRuleActivity.RuleParcelableEvent ->
                        val intent = Intent(this, EditRuleActivity::class.java).putExtra("rule_event", rule)
                        startActivity(intent)
                        finish()
                    },
                    onCancelRule = {
                            rule: Rule ->
                        viewModel.cancelRule(rule, this)
                    },
                )
            }
        }
    }
}
