package com.tasa.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Parcelable
import android.util.Log
import androidx.core.net.toUri
import com.tasa.domain.Action
import com.tasa.domain.TriggerTime
import com.tasa.domain.toTriggerTime
import com.tasa.repository.TasaRepo
import com.tasa.silence.MuteReceiver
import java.util.Calendar
import kotlin.time.Duration.Companion.minutes

class AlarmScheduler(
    private val repo: TasaRepo,
) {
    suspend fun scheduleAlarm(
        time: TriggerTime,
        action: Action,
        context: Context,
    ) {
        val alarmId = repo.alarmRepo.createAlarm(time.value, action)
        val intent =
            Intent(context, MuteReceiver::class.java).apply {
                putExtra("action", action as Parcelable)
                data = "custom://alarm/$alarmId".toUri() // garante que o PendingIntent é único
            }
        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                alarmId,
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
            Log.d("Alarm", "Alarme agendado para ${calendar.time}")
            alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
        } catch (e: Throwable) {
            Log.d("Alarm", "Erro ao agendar alarme: ${e.message}")
        }
    }

    suspend fun updateAlarm(
        alarmId: Int,
        time: TriggerTime,
        action: Action,
        context: Context,
    ) {
        repo.alarmRepo.updateAlarm(time.value, action, alarmId)
        val intent =
            Intent(context, MuteReceiver::class.java).apply {
                putExtra("action", action as Parcelable)
                data = "custom://alarm/$alarmId".toUri()
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                alarmId,
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
        val alarmMgr = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmMgr.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent,
        )
    }

    suspend fun cancelAlarm(
        alarmId: Int,
        context: Context,
    ) {
        repo.alarmRepo.deleteAlarm(alarmId)

        val intent =
            Intent(context, MuteReceiver::class.java).apply {
                data = "custom://alarm/$alarmId".toUri()
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                alarmId,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

        val alarmMgr = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmMgr.cancel(pendingIntent)
    }

    suspend fun rescheduleAllAlarms(context: Context) {
        val alarms = repo.alarmRepo.getAllAlarms()
        val now = Calendar.getInstance().timeInMillis
        alarms.filter {
            it.triggerTime >= now ||
                it.triggerTime < now.minus(10.minutes.inWholeMinutes)
        }.forEach { alarm ->
            scheduleAlarm(alarm.triggerTime.toTriggerTime(), alarm.action, context)
        }
    }
}
