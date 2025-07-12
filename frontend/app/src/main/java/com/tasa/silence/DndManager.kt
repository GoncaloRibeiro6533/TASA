package com.tasa.silence

import android.app.NotificationManager
import android.util.Log

/**
 * DndManager is a utility class that manages the Do Not Disturb (DND) mode of the device.
 * It provides methods to mute, unmute, and check the current DND status.
 */

object DndManager {
    fun mute(notificationManager: NotificationManager) {
        if (notificationManager.isNotificationPolicyAccessGranted) {
            Log.d("Alarm", "Muting")
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        }
    }

    fun unmute(notificationManager: NotificationManager) {
        if (notificationManager.isNotificationPolicyAccessGranted) {
            Log.d("Alarm", "Unmuting")
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
    }

    fun isMuted(notificationManager: NotificationManager): Boolean {
        return if (notificationManager.isNotificationPolicyAccessGranted) {
            when (notificationManager.currentInterruptionFilter) {
                NotificationManager.INTERRUPTION_FILTER_NONE,
                NotificationManager.INTERRUPTION_FILTER_PRIORITY,
                NotificationManager.INTERRUPTION_FILTER_ALARMS,
                -> true

                else -> false
            }
        } else {
            false
        }
    }
}
