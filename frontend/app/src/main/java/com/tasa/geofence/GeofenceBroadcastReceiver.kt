package com.tasa.geofence

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.tasa.DependenciesContainer
import com.tasa.silence.DndManager

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        Log.d("GeofenceBroadcastReceiver", "onReceive:")
        val geofencingEvent =
            GeofencingEvent.fromIntent(intent)
                ?: return
        val geofenceTransition = geofencingEvent.geofenceTransition
        Log.d("GeofenceBroadcastReceiver", "geofencingEvent: $geofenceTransition")
        if (geofencingEvent.hasError()) {
            val errorMessage =
                GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            return
        }
        geofencingEvent.triggeringLocation
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
        DndManager.mute(notificationManager)

        val repo = (context.applicationContext as DependenciesContainer).repo
        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                val requestId =
                    geofencingEvent.triggeringGeofences?.firstOrNull()?.requestId
                        ?: return
                DndManager.mute(notificationManager)
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                DndManager.unmute(notificationManager)
            }

            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                // "→ Está dentro há algum tempo (dwell)")
                // Só se tiveres configurado este tipo de trigger
            }

            else -> {
            }
        }
    }
}
