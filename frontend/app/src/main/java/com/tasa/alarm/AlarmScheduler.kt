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

/**
 * Manages scheduling, updating, and canceling alarms using Android's AlarmManager.
 * It creates unique PendingIntents for each alarm based on the alarm ID and action.
 *
 * @property context The application context used to access system services.
 */
class AlarmScheduler(
    private val context: Context,
) {
    private val alarmMgr: AlarmManager by lazy {
        context.getSystemService(AlarmManager::class.java) as AlarmManager
    }

    /**
     * Creates a unique PendingIntent for the given alarm ID and action.
     * The intent includes the action as an extra and uses a custom URI to ensure uniqueness.
     *
     * @param alarmId The unique identifier for the alarm.
     * @param action The action to be performed when the alarm triggers.
     * @return A PendingIntent that can be used to trigger the alarm.
     */
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

    /**
     * Schedules an alarm with the specified ID, time, and action.
     * It creates a PendingIntent and sets the alarm using AlarmManager.
     *
     * @param alarmId The unique identifier for the alarm.
     * @param time The time at which the alarm should trigger.
     * @param action The action to be performed when the alarm triggers.
     */
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

    /**
     * Updates an existing alarm by canceling it and scheduling a new one with the same ID.
     * This is useful for changing the time or action of an existing alarm.
     *
     * @param alarmId The unique identifier for the alarm to be updated.
     * @param time The new time at which the alarm should trigger.
     * @param action The new action to be performed when the alarm triggers.
     */
    fun updateAlarm(
        alarmId: Int,
        time: TriggerTime,
        action: Action,
    ) {
        cancelAlarm(alarmId, action)
        scheduleAlarm(alarmId, time, action)
    }

    /**
     * Cancels an existing alarm by its ID and action.
     * It retrieves the PendingIntent associated with the alarm and cancels it.
     *
     * @param alarmId The unique identifier for the alarm to be canceled.
     * @param action The action associated with the alarm to be canceled.
     */
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

    /**
     * Reschedules all alarms that are either in the future or have not been triggered in the last 10 minutes.
     * This is useful for ensuring that alarms are still active and correctly set.
     *
     * @param alarms A list of alarms to be rescheduled.
     */
    fun rescheduleAllAlarms(alarms: List<Alarm>) {
        val now = Calendar.getInstance().timeInMillis
        alarms.filter {
            it.triggerTime >= now || it.triggerTime < now.minus(10.minutes.inWholeMilliseconds)
        }.forEach { alarm ->
            scheduleAlarm(alarm.id, alarm.triggerTime.toTriggerTime(), alarm.action)
        }
    }
}
