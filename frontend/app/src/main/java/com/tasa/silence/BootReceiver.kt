package com.tasa.silence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tasa.DependenciesContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent?,
    ) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val alarmScheduler = (context.applicationContext as DependenciesContainer).ruleScheduler
            CoroutineScope(Dispatchers.IO).launch {
                alarmScheduler.rescheduleAllAlarms(context)
            }
            val activityTransitionManager =
                (context.applicationContext as DependenciesContainer).activityTransitionManager
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (context.checkSelfPermission(android.Manifest.permission.ACTIVITY_RECOGNITION)
                        == android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        activityTransitionManager.registerActivityTransitions()
                    }
                } catch (e: SecurityException) {
                    // Permissão não concedida, lidar conforme necessário
                }
            }
        }
    }
}
