package com.tasa.geofence

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

const val TAG = "GeofenceManager"
const val CUSTOM_REQUEST_CODE_GEOFENCE = 1001

class GeofenceManager(
    private val context: Context,
    private val locationProviderClient: FusedLocationProviderClient,
) {
    private val client = LocationServices.getGeofencingClient(context)

    private val geofencingPendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)

        PendingIntent.getBroadcast(
            context,
            CUSTOM_REQUEST_CODE_GEOFENCE,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            },
        )
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun registerGeofence(
        key: String,
        location: Location,
        radiusInMeters: Float = 100.0f,
        expirationTimeInMillis: Long = Geofence.NEVER_EXPIRE,
    ) {
        client.addGeofences(
            createGeofencingRequest(
                createGeofence(key, location, radiusInMeters, expirationTimeInMillis),
            ),
            geofencingPendingIntent,
        )
            .addOnSuccessListener { result ->
                Log.d(TAG, "registerGeofence: SUCCESS")
            }.addOnFailureListener { exception ->
                Log.d(TAG, "registerGeofence: Failure\n$exception")
            }
    }

    suspend fun deregisterGeofence(requestId: String) =
        runCatching {
            client.removeGeofences(listOf(requestId)).await()
        }

    private fun createGeofencingRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER or GeofencingRequest.INITIAL_TRIGGER_EXIT)
            addGeofence(geofence)
        }.build()
    }

    private fun createGeofence(
        key: String,
        location: Location,
        radiusInMeters: Float,
        expirationTimeInMillis: Long = Geofence.NEVER_EXPIRE,
    ): Geofence {
        return Geofence.Builder()
            .setRequestId(key)
            .setCircularRegion(location.latitude, location.longitude, radiusInMeters)
            .setExpirationDuration(expirationTimeInMillis)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    suspend fun onBootRegisterGeofences(list: List<com.tasa.repository.interfaces.Geofence>) {
        val result =
            list.map {
                    geofenceEntity ->
                if (deregisterGeofence(geofenceEntity.name).isSuccess) {
                    registerGeofence(
                        key = geofenceEntity.name,
                        location =
                            Location("").apply {
                                latitude = geofenceEntity.latitude
                                longitude = geofenceEntity.longitude
                            },
                        radiusInMeters = geofenceEntity.radius.toFloat(),
                    )
                }
                geofenceEntity
            }
        if (result.isNotEmpty()) {
            getCurrentLocation()
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private suspend fun getCurrentLocation() {
        val priority = Priority.PRIORITY_HIGH_ACCURACY
        locationProviderClient.getCurrentLocation(
            priority,
            CancellationTokenSource().token,
        ).await()
    }
}
