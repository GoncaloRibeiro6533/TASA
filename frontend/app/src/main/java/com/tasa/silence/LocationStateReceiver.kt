package com.tasa.silence

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.tasa.DependenciesContainer
import com.tasa.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LocationStateReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        Log.d("LocationStateReceiver", "onReceive: ${intent.action}")
        if (intent.action == android.location.LocationManager.PROVIDERS_CHANGED_ACTION) {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager

            val isGpsEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)

            if (isGpsEnabled) {
                Log.d("LocationStateReceiver", "Location is back ON")
                val geofenceManager = (context.applicationContext as DependenciesContainer).geofenceManager
                CoroutineScope(Dispatchers.IO).launch {
                    if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.d("LocationStateReceiver", "Re-registering geofences")
                        geofenceManager.onBootRegisterGeofences()
                    }
                }
            } else {
                showLocationDisabledNotification(context)
            }
        }
    }

    fun createLocationWarningNotificationChannel(context: Context) {
        val channel =
            NotificationChannel(
                "location_warning_channel",
                "Avisos de Localização",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Notificações importantes sobre localização desativada"
            }

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun showLocationDisabledNotification(context: Context) {
        createLocationWarningNotificationChannel(context)

        val notification =
            NotificationCompat.Builder(context, "location_warning_channel")
                .setSmallIcon(R.drawable.tasa_logo) // usa o teu ícone
                .setContentTitle("Localização Desativada")
                .setContentText("A localização está desligada. O silenciamento automático não vai funcionar.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(42, notification)
    }
}
