package com.tasa.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityRecognitionResult
import com.tasa.DependenciesContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A BroadcastReceiver that listens for activity recognition updates.
 * It extracts the most probable activity from the intent and writes it to the repository.
 */
class UserActivityReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val repo = (context.applicationContext as DependenciesContainer).userInfoRepository
        val result = ActivityRecognitionResult.extractResult(intent) ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val lastEvent = result.mostProbableActivity
                repo.writeLastActivity(lastEvent.type)
            } catch (e: Throwable) {
            }
        }
    }
}
