package com.tasa.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.tasks.await

enum class Activity(val type: Int) {
    IN_VEHICLE(0),
    ON_BICYCLE(1),
    ON_FOOT(2),
    STILL(3),
    UNKNOWN(4),
    TILTING(5),
    WALKING(7),
    RUNNING(8),
}

class UserActivityTransitionManager(private val context: Context) {
    companion object {
        fun getActivityType(int: Int?): String {
            return when (int) {
                0 -> "IN_VEHICLE"
                1 -> "ON_BICYCLE"
                2 -> "ON_FOOT"
                3 -> "STILL"
                4 -> "UNKNOWN"
                5 -> "TILTING"
                7 -> "WALKING"
                8 -> "RUNNING"
                else -> "UNKNOWN"
            }
        }

        fun getTransitionType(int: Int): String {
            return when (int) {
                0 -> "STARTED"
                1 -> "STOPPED"
                else -> ""
            }
        }
    }

    // list of activity transitions to be monitored
    private val activityTransitions: List<ActivityTransition> by lazy {
        listOf(
            getUserActivity(
                DetectedActivity.IN_VEHICLE,
                ActivityTransition.ACTIVITY_TRANSITION_ENTER,
            ),
            getUserActivity(
                DetectedActivity.IN_VEHICLE,
                ActivityTransition.ACTIVITY_TRANSITION_EXIT,
            ),
            getUserActivity(
                DetectedActivity.ON_BICYCLE,
                ActivityTransition.ACTIVITY_TRANSITION_ENTER,
            ),
            getUserActivity(
                DetectedActivity.ON_BICYCLE,
                ActivityTransition.ACTIVITY_TRANSITION_EXIT,
            ),
            getUserActivity(
                DetectedActivity.WALKING,
                ActivityTransition.ACTIVITY_TRANSITION_ENTER,
            ),
            getUserActivity(
                DetectedActivity.WALKING,
                ActivityTransition.ACTIVITY_TRANSITION_EXIT,
            ),
            getUserActivity(
                DetectedActivity.RUNNING,
                ActivityTransition.ACTIVITY_TRANSITION_ENTER,
            ),
            getUserActivity(
                DetectedActivity.RUNNING,
                ActivityTransition.ACTIVITY_TRANSITION_EXIT,
            ),
            getUserActivity(
                DetectedActivity.STILL,
                ActivityTransition.ACTIVITY_TRANSITION_ENTER,
            ),
            getUserActivity(
                DetectedActivity.STILL,
                ActivityTransition.ACTIVITY_TRANSITION_EXIT,
            ),
        )
    }

    private val activityClient = ActivityRecognition.getClient(context)

    val intent =
        Intent(context, UserActivityReceiver::class.java).apply {
            action = "com.tasa.ACTIVITY_RECOGNITION"
        }
    private val pendingIntent by lazy {
        PendingIntent.getBroadcast(
            context,
            2007,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
        )
    }

    private fun getUserActivity(
        detectedActivity: Int,
        transitionType: Int,
    ): ActivityTransition {
        return ActivityTransition.Builder().setActivityType(detectedActivity)
            .setActivityTransition(transitionType).build()
    }

    @RequiresPermission(
        anyOf = [
            Manifest.permission.ACTIVITY_RECOGNITION,
            "com.google.android.gms.permission.ACTIVITY_RECOGNITION",
        ],
    )
    suspend fun registerActivityTransitions() =
        runCatching {
            if (context.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
                activityClient.requestActivityUpdates(500L, pendingIntent).await()
            } else {
                throw SecurityException("ACTIVITY_RECOGNITION permission not granted")
            }
        }.onFailure {
            Log.e("UserActivityTransitionManager", "Failed to register activity transitions", it)
        }

    @SuppressLint("InlinedApi")
    @RequiresPermission(
        anyOf = [
            Manifest.permission.ACTIVITY_RECOGNITION,
            "com.google.android.gms.permission.ACTIVITY_RECOGNITION",
        ],
    )
    suspend fun deregisterActivityTransitions() =
        runCatching {
            activityClient.removeActivityTransitionUpdates(pendingIntent).await()
        }
}
