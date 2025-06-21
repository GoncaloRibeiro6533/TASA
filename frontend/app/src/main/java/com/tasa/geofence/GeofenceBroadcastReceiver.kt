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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

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
                CoroutineScope(Dispatchers.IO).launch {
                  /*  val rule = repo.ruleRepo.fetchRuleLocationsByName(requestId)
                    if (rule.isNotEmpty()) {
                        val now = LocalDateTime.now()
                        val filteredRules = rule.filter {
                            it.startTime.isBefore(now) && it.endTime.isAfter(now)
                        }
                        if (filteredRules.isNotEmpty()) {
                            // se entra no geofence*/
                    DndManager.mute(notificationManager)
                    // }
                    // }
                }
            }

            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                val requestId =
                    geofencingEvent.triggeringGeofences?.firstOrNull()?.requestId
                        ?: return
                CoroutineScope(Dispatchers.IO).launch {
                    val rule = repo.ruleRepo.fetchRuleLocationsByName(requestId)
                    if (rule.isNotEmpty()) {
                        val now = LocalDateTime.now()
                        val filteredRules =
                            rule.filter {
                                it.startTime.isBefore(now) && it.endTime.isAfter(now)
                            }
                        if (filteredRules.isNotEmpty()) {
                            // se entra no geofence
                            DndManager.unmute(notificationManager)
                        }
                    }
                }
            }

            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                // "→ Está dentro há algum tempo (dwell)")
                // Só se tiveres configurado este tipo de trigger
            }

            else -> {
            }
        }

       /* val location = geofencingEvent.triggeringLocation
        val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return

         check if any rule are set for this time
        val repo = (context.applicationContext as DependenciesContainer).repo


        for (geofence in triggeringGeofences) {
            val requestId = geofence.requestId  // ← Este é o identificador que você criou
        */

        // Agora você pode consultar no repositório usando o requestId
        // Aja com base na regra
    }
}
