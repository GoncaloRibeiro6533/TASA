package com.tasa.newlocation

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
        // if( ActivityTransitionResult.hasResult(intent)) {
        val repo = (context.applicationContext as DependenciesContainer).userInfoRepository
        val result = ActivityRecognitionResult.extractResult(intent) ?: return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val lastEvent = result.mostProbableActivity
                   /* for (event in result.transitionEvents) {
                        Log.d("UserActivityReceiver", "ActivityType recebido: ${event.activityType}")
                        Log.d("UserActivityReceiver", "TransitionType recebido: ${event.transitionType}")

                        val activityType = UserActivityTransitionManager.getActivityType(event.activityType)
                        val transitionType = UserActivityTransitionManager.getTransitionType(event.transitionType)
                        Log.d("UserActivityReceiver", "Atividade: $activityType, Transição: $transitionType")
                    }*/
                Log.d("UserActivityReceiver", "Último evento: Atividade: $lastEvent, Type: ${lastEvent.type}")
                repo.writeLastActivity(lastEvent.type)
                // repo.writeLastActivityTransition(lastEvent.transitionType)
            } catch (e: Throwable) {
                Log.e("UserActivityReceiver", "Erro a gravar transição", e)
            }
        }
        // } else {
        //   Log.w("UserActivityReceiver", "Intent não contém ActivityTransitionResult")
        // }
    }
}
