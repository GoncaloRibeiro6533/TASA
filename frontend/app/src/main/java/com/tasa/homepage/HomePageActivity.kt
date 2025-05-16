package com.tasa.homepage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tasa.calendar.CalendarActivity
import com.tasa.newevent.NewEvenActivity
import com.tasa.newlocation.MapActivity
import com.tasa.ui.theme.TasaTheme
import com.tasa.utils.navigateTo


class HomePageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TasaTheme {
                HomePageScreen(
                    onNavigationToMap = {navigateTo(this, MapActivity::class.java)},
                    onNavigationToNewEvent = {navigateTo(this, NewEvenActivity::class.java)},
                    onNavigateToMyEvents = {navigateTo(this, CalendarActivity::class.java)}
                )
            }
        }
    }
}
