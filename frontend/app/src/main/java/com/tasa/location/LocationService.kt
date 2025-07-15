package com.tasa.location

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
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

/**
 * LocationService is a foreground service that listens for location updates
 * and manages Do Not Disturb (DND) mode based on the user's location.
 * It checks if the user is within a specified radius of a predefined location
 * and mutes or unmutes the device accordingly.
 */
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

    /**
     * Called when the service is started. It retrieves the location by requestId,
     * starts listening for location updates, and manages DND mode based on the user's location.
     *
     * @param intent The Intent that started the service, containing the requestId.
     * @param flags Additional flags about how the service is started.
     * @param startId A unique integer representing this specific request to start.
     * @return An integer indicating how the system should continue the service if it is killed.
     */
    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        try {
            isRunning = true
            val notification = buildNotification()
            startForeground(NOTIFICATION_ID, notification)
            val requestId = intent?.getStringExtra("requestId").toString()
            reqId = requestId
            locationName = requestId
            // get the location by requestId
            scope.launch {
                val result = repo.locationRepo.getLocationByName(requestId)
                if (result != null) {
                    radius = result.radius.toFloat()
                    locationOfSilence = result.toLocation()
                    listenToLocationUpdates()
                } else {
                    onDestroy()
                }
            }
            return START_STICKY // Ensures the system restarts the service if it's killed
        } catch (e: Exception) {
            onDestroy()
            return START_NOT_STICKY // Stops the service if an error occurs
        }
    }

    /**
     * Listens for location updates and manages DND mode based on the user's location.
     * If the user is within the specified radius of the predefined location,
     * it mutes the device; otherwise, it unmutes the device.
     */
    @RequiresPermission(
        allOf = [
            "android. permission. ACCESS_FINE_LOCATION",
            "android. permission. ACCESS_COARSE_LOCATION",
            "android. permission. ACTIVITY_RECOGNITION",
        ],
    )
    private suspend fun listenToLocationUpdates() {
        val activityPermission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Manifest.permission.ACTIVITY_RECOGNITION
            } else {
                "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
            }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                activityPermission,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationUpdates.startUp()
        val locationOfSilence: Location = locationOfSilence ?: return
        locationUpdates.centralLocationFlow.collect { location ->
            if (location != null) {
                if (location.toLocation().distanceTo(locationOfSilence) - location.accuracy <= radius) {
                    if (!DndManager.isMuted(notificationManager)) {
                        DndManager.mute(notificationManager)
                    }
                } else {
                    if (DndManager.isMuted(notificationManager)) {
                        DndManager.unmute(notificationManager)
                    }
                }
            }
        }
    }

    /**
     * Called when the service is destroyed. It stops location updates,
     * unmutes the device, and cancels the coroutine scope.
     */
    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        DndManager.unmute(notificationManager)
        locationUpdates.stop()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Builds a notification for the foreground service.
     * It sets the content title, small icon, and priority.
     *
     * @return A Notification object for the foreground service.
     */
    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.tasa_is_executing_in_the_backgrund))
            .setSmallIcon(R.drawable.tasa_logo)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /**
     * Creates a notification channel for the foreground service.
     * This is required for Android O and above to display notifications.
     */
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
