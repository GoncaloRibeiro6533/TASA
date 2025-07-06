package com.tasa.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class Synchronizer(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val repo = (context.applicationContext as com.tasa.DependenciesContainer).repo
            // events synchronization
            repo.eventRepo.syncEvents()
            repo.locationRepo.syncLocations()
            // repo.ruleRepo.syncRules()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
