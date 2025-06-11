package com.tasa.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val geofencingEvent =
            GeofencingEvent.fromIntent(intent)
                ?: return
        if (geofencingEvent.hasError()) {
            val errorMessage =
                GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            return
        }
        val geofenceTransition = geofencingEvent.geofenceTransition
        val location = geofencingEvent.triggeringLocation
        val triggeringGeofences = geofencingEvent.triggeringGeofences

        // check if any rule are set for this time
    }
}
