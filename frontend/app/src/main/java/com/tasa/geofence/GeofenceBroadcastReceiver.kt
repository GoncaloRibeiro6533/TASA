package com.tasa.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.tasa.location.LocationService

/**
 * A BroadcastReceiver that listens for geofence transitions.
 * It starts or stops the LocationService based on the type of geofence transition.
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val geofencingEvent =
            GeofencingEvent.fromIntent(intent)
                ?: return
        val geofenceTransition = geofencingEvent.geofenceTransition
        if (geofencingEvent.hasError()) {
            return
        }
        val triggeringLocation = geofencingEvent.triggeringLocation
        val serviceIntent =
            Intent(context, LocationService::class.java).apply {
                putExtra("lat", triggeringLocation?.latitude)
                putExtra("lon", triggeringLocation?.longitude)
                putExtra("accuracy", triggeringLocation?.accuracy)
                putExtra("requestId", geofencingEvent.triggeringGeofences?.get(0)?.requestId)
            }
        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                context.startForegroundService(serviceIntent)
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                context.stopService(serviceIntent)
            }

            Geofence.GEOFENCE_TRANSITION_DWELL -> {
            }
        }
    }
}
