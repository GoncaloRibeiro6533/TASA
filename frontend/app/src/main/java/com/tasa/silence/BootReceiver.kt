package com.tasa.silence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tasa.DependenciesContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A BroadcastReceiver that listens for the BOOT_COMPLETED action.
 * It reschedules alarms, registers activity transitions, and geofences after the device boots up.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent?,
    ) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val alarmScheduler = (context.applicationContext as DependenciesContainer).ruleScheduler
            val scope = CoroutineScope(Dispatchers.IO)
            val repo = (context.applicationContext as DependenciesContainer).repo
            scope.launch {
                alarmScheduler.rescheduleAllAlarms(repo.alarmRepo.getAllAlarms())
            }
            val activityTransitionManager =
                (context.applicationContext as DependenciesContainer).activityTransitionManager
            scope.launch {
                try {
                    if (context.checkSelfPermission(android.Manifest.permission.ACTIVITY_RECOGNITION)
                        == android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        activityTransitionManager.registerActivityTransitions()
                    }
                } catch (e: SecurityException) {
                }
            }
            val geofenceManager =
                (context.applicationContext as DependenciesContainer).geofenceManager
            scope.launch {
                try {
                    geofenceManager.onBootRegisterGeofences(repo.geofenceRepo.getAllGeofences())
                } catch (e: SecurityException) {
                }
            }
        }
    }
}
