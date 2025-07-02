package com.tasa.silence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.tasa.DependenciesContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent?,
    ) {
        Log.d("BootReceiver", "onReceive: BOOT_COMPLETED")
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val alarmScheduler = (context.applicationContext as DependenciesContainer).ruleScheduler
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                alarmScheduler.rescheduleAllAlarms(context)
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
                    geofenceManager.onBootRegisterGeofences()
                } catch (e: SecurityException) {
                }
            }
        }
    }
}
