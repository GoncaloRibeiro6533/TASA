package com.tasa.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import androidx.core.net.toUri
import com.tasa.domain.Action
import com.tasa.domain.Alarm
import com.tasa.domain.TriggerTime
import com.tasa.domain.toTriggerTime
import com.tasa.silence.MuteReceiver
import java.util.Calendar
import kotlin.time.Duration.Companion.minutes

class AlarmScheduler(
    private val context: Context,
) {
    private val alarmMgr: AlarmManager by lazy {
        context.getSystemService(AlarmManager::class.java) as AlarmManager
    }

    private fun createPendingIntent(
        alarmId: Int,
        action: Action,
    ): PendingIntent {
        val intent =
            Intent(context, MuteReceiver::class.java).apply {
                putExtra("action", action as Parcelable)
                data = "custom://alarm/$alarmId".toUri() // garante que o PendingIntent é único
            }

        return PendingIntent.getBroadcast(
            context,
            alarmId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    fun scheduleAlarm(
        alarmId: Int,
        time: TriggerTime,
        action: Action,
    ) {
        val pendingIntent = createPendingIntent(alarmId, action)

        val calendar =
            Calendar.getInstance().apply {
                set(Calendar.YEAR, time.year)
                set(Calendar.MONTH, time.month)
                set(Calendar.DAY_OF_MONTH, time.day)
                set(Calendar.HOUR_OF_DAY, time.hour)
                set(Calendar.MINUTE, time.minute)
                set(Calendar.SECOND, time.second)
            }
        alarmMgr.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent,
        )
    }

    fun updateAlarm(
        alarmId: Int,
        time: TriggerTime,
        action: Action,
    ) {
        cancelAlarm(alarmId, action)
        scheduleAlarm(alarmId, time, action)
    }

    fun cancelAlarm(
        alarmId: Int,
        action: Action,
    ) {
        Log.d("AlarmDebug", "Cancelando alarme: alarmId=$alarmId, action=${action.name}")
        val intent =
            Intent(context, MuteReceiver::class.java).apply {
                this.data = "custom://alarm/$alarmId".toUri()
            }
        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                alarmId,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            )
        alarmMgr.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    fun rescheduleAllAlarms(alarms: List<Alarm>) {
        val now = Calendar.getInstance().timeInMillis
        alarms.filter {
            it.triggerTime >= now || it.triggerTime < now.minus(10.minutes.inWholeMilliseconds)
        }.forEach { alarm ->
            scheduleAlarm(alarm.id, alarm.triggerTime.toTriggerTime(), alarm.action)
        }
    }
}
