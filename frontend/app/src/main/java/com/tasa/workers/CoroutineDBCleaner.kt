package com.tasa.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tasa.DependenciesContainer
import com.tasa.calendar.toLocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CoroutineDBCleaner(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repo = (applicationContext as DependenciesContainer).repo
        return withContext(Dispatchers.IO) {
            try {
                val now = System.currentTimeMillis()
                repo.alarmRepo.clearOlderAlarms(now)
                repo.ruleRepo.cleanOldRules(now.toLocalDateTime())
                // clear event not associated with any rule TODO
                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }
}



