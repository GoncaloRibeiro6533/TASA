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
           /* val uri = intent.data
            val actionName = uri?.lastPathSegment
            val action = Action.valueOf(actionName ?: return)
            Log.d("Alarm", "Ação recebida: $action")*/
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
