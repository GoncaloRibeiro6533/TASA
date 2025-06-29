package com.tasa.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tasa.DependenciesContainer
import com.tasa.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class LocationStatusWorker(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val isGpsEnabled = isLocationEnabled(context)
        val repo =
            (context.applicationContext as DependenciesContainer).userInfoRepository
        return withContext(Dispatchers.IO) {
            try {
                if (LocalDateTime.now().hour in 0..7) {
                    return@withContext Result.success()
                }
                if (isGpsEnabled && repo.getLocationStatus() == false) {
                    Log.d("LocationStateReceiver", "Location is back ON")
                    val geofenceManager = (context.applicationContext as DependenciesContainer).geofenceManager
                    if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                        == android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        repo.setLocationStatus(true)
                        Log.d("LocationStateReceiver", "Re-registering geofences")
                        geofenceManager.onBootRegisterGeofences()
                    }
                } else {
                    if (repo.getLocationStatus() == true && !isGpsEnabled) {
                        repo.setLocationStatus(false)
                        showLocationDisabledNotification(context)
                    }
                }
                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }
}

private fun isLocationEnabled(context: Context): Boolean {
    return try {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            @Suppress("DEPRECATION")
            Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE) != Settings.Secure.LOCATION_MODE_OFF
        }
    } catch (e: Settings.SettingNotFoundException) {
        false
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
            .setSmallIcon(R.drawable.tasa_logo)
            .setContentTitle("Localização Desativada")
            .setContentText("A localização está desligada. O silenciamento automático não vai funcionar.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(42, notification)
}
