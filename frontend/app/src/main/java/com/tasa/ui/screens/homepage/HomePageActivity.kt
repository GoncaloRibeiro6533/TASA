package com.tasa.ui.screens.homepage

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.tasa.DependenciesContainer
import com.tasa.calendar.CalendarActivity
import com.tasa.domain.Rule
import com.tasa.newlocation.MapActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadLocalData()
        setContent {
            TasaTheme {
                HomePageScreen(
                    viewModel = viewModel,
                    onNavigateToCreateRuleEvent = {
                        finish()
                        navigateTo(this, CalendarActivity::class.java)
                    },
                    onNavigationToMap = {
                        startActivity(Intent(this, MapActivity::class.java))
                        // finish()  TODO
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
                    onFatalError = { finish() }, // TODO close app
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
