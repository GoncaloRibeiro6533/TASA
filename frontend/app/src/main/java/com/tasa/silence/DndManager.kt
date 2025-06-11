package com.tasa.silence

import android.app.NotificationManager
import android.util.Log

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
}
