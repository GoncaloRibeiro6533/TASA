package com.tasa.silence

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import com.tasa.domain.Action
import com.tasa.domain.Mode

class MuteReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
            return
        }
        try {
            when (intent.getParcelableExtra("action", Action::class.java)) {
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
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        }
    }

    private fun unmute(notificationManager: NotificationManager) {
        if (notificationManager.isNotificationPolicyAccessGranted) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
    }

    private fun AudioManager.getCurrentMode(): Mode {
        return when (ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> Mode.SILENT
            AudioManager.RINGER_MODE_VIBRATE -> Mode.VIBRATE
            AudioManager.RINGER_MODE_NORMAL -> Mode.RINGING
            else -> Mode.RINGING
        }
    }
}
