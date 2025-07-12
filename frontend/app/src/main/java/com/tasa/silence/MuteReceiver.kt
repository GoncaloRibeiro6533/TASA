package com.tasa.silence

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import com.tasa.domain.Action

/**
 *  MuteReceiver is a BroadcastReceiver that listens for alarms to trigger so
 *  that it can mute or unmute the device.
 * It checks if the app has notification policy access and performs the corresponding action.
 * If access is not granted, it redirects the user to the settings page.
 */
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
                    DndManager.mute(notificationManager)
                }
                Action.UNMUTE -> {
                    DndManager.unmute(notificationManager)
                }

                null -> return
            }
            Log.d("Alarm", "Alarme disparado")
        } catch (ex: Throwable) {
            Log.e("Alarm", "Erro ao agendar alarme: ${ex.message}")
        }
    }
}
