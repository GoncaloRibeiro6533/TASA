package com.tasa.location

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.tasa.DependenciesContainer
import com.tasa.R
import com.tasa.silence.DndManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class LocationService : Service() {
    companion object {
        const val CHANNEL_ID = "location_channel"
        const val NOTIFICATION_ID = 1
        var isRunning = false
        var locationName: String? = null
        var locationOfSilence: Location? = null
        var radius: Float = 0f
        var reqId: String? = null
    }

    private val locationUpdates by lazy {
        (application as DependenciesContainer).locationUpdatesRepository
    }
    private val repo by lazy {
        (application as DependenciesContainer).repo
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    private val notificationManager by lazy {
        this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    @RequiresPermission(
        allOf = [
            "android. permission. ACTIVITY_RECOGNITION",
            "android. permission. ACCESS_COARSE_LOCATION",
            "android. permission. ACCESS_FINE_LOCATION",
        ],
    )
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        Log.d("GeofenceBroadcastReceiver", "onStartCommand:")
        isRunning = true
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)
        Log.d("LocationService", "Service started in foreground")
        val requestId = intent?.getStringExtra("requestId").toString()
        reqId = requestId
        locationName = requestId
        // get the location by requestId
        scope.launch {
            val result = repo.locationRepo.getLocationByName(requestId)
            Log.d("LocationService", "RequestId: $requestId, Result: $result")
            if (result != null) {
                radius = result.radius.toFloat()
                locationOfSilence = result.toLocation()
                listenToLocationUpdates()
            } else {
                Log.e("LocationService", "Location not found for requestId: |$requestId|")
                onDestroy()
            }
        }
        return START_STICKY // Ensures the system restarts the service if it's killed
    }

    @RequiresPermission(
        allOf = [
            "android. permission. ACCESS_FINE_LOCATION",
            "android. permission. ACCESS_COARSE_LOCATION",
            "android. permission. ACTIVITY_RECOGNITION",
        ],
    )
    private suspend fun listenToLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationUpdates.startUp()
        val locationOfSilence: Location = locationOfSilence ?: return
        locationUpdates.centralLocationFlow.collect { location ->
            Log.d("LocationService", "Location: $location")
            if (location != null) {
                // check if the location TODO
                if (location.toLocation().distanceTo(locationOfSilence) <= radius + 30 // &&
                    // !DndManager.isMuted(notificationManager)
                ) {
                    DndManager.mute(notificationManager)
                } else {
                    // if (DndManager.isMuted(notificationManager)) {
                    DndManager.unmute(notificationManager)
                    // }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        Log.d("LocationService", "Service destroyed")
        DndManager.unmute(notificationManager)
        locationUpdates.stop()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Tasa está a monitorizar localização")
            .setContentText("A app está ativa para geofencing.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        val serviceChannel =
            NotificationChannel(
                CHANNEL_ID,
                "Location Service Channel",
                NotificationManager.IMPORTANCE_LOW,
            )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(serviceChannel)
    }
}
