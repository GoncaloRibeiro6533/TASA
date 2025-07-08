package com.tasa.workers

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tasa.DependenciesContainer

class Synchronizer(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override suspend fun doWork(): Result {
        return try {
            val repo = (context.applicationContext as DependenciesContainer).repo
            repo.ruleRepo.syncRules()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
