package com.tasa.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import com.tasa.domain.Action
import com.tasa.domain.TriggerTime
import com.tasa.repository.TasaRepo
import com.tasa.silence.MuteReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmScheduler(
    private val repo: TasaRepo,
) {
    fun scheduleAlarm(
        time: TriggerTime,
        action: Action,
        context: Context,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val alarm = repo.alarmRepo.createAlarm(time.value, action)
            val intent = Intent(context, MuteReceiver::class.java).putExtra("action", action)
            val pendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    alarm,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
            val calendar =
                Calendar.getInstance().apply {
                    set(Calendar.YEAR, time.year)
                    set(Calendar.MONTH, time.month)
                    set(Calendar.DAY_OF_MONTH, time.day)
                    set(Calendar.HOUR_OF_DAY, time.hour)
                    set(Calendar.MINUTE, time.minute)
                    set(Calendar.SECOND, time.second)
                }
            try {
                val alarmMgr = context.getSystemService(ALARM_SERVICE) as AlarmManager
                alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }
}
