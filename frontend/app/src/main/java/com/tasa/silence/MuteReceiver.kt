package com.tasa.silence

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import com.tasa.domain.Action

class MuteReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
        try {
            when (intent.getParcelableExtra<Action>("action")) {
                Action.MUTE -> {
                    mute(notificationManager)
                }
                Action.UNMUTE -> {
                    unmute(notificationManager)
                }

                null -> {}
            }
            Log.d("Alarm", "Alarme disparado")
        } catch (ex: Throwable) {
            Log.e("Alarm", "Erro ao agendar alarme: ${ex.message}")
        }
    }

    private fun mute(notificationManager: NotificationManager) {
        if (notificationManager.isNotificationPolicyAccessGranted) {
            Log.d("Alarm", "Muting")
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        }
    }

    private fun unmute(notificationManager: NotificationManager) {
        if (notificationManager.isNotificationPolicyAccessGranted) {
            Log.d("Alarm", "Unmuting")
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
    }
}
