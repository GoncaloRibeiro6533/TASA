package com.tasa.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityRecognitionResult
import com.tasa.DependenciesContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserActivityReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        Log.d("UserActivityReceiver", "Intent recebida: ${intent.action}")
        val repo = (context.applicationContext as DependenciesContainer).userInfoRepository
        val result = ActivityRecognitionResult.extractResult(intent) ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val lastEvent = result.mostProbableActivity
                repo.writeLastActivity(lastEvent.type)
                // repo.writeLastActivityTransition(lastEvent.transitionType)
            } catch (e: Throwable) {
                Log.e("UserActivityReceiver", "Erro a gravar transição", e)
            }
        }
    }
}
