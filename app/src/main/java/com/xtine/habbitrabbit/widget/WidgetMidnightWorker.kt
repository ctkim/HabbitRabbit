package com.xtine.habbitrabbit.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class WidgetMidnightWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        HabbitRabbitWidget().updateAll(applicationContext)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "widget_midnight_refresh"

        fun enqueue(context: Context) {
            val delay = durationUntilNextMidnight()
            val request = PeriodicWorkRequestBuilder<WidgetMidnightWorker>(Duration.ofDays(1))
                .setInitialDelay(delay)
                .setConstraints(Constraints.Builder().build())
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        private fun durationUntilNextMidnight(): Duration {
            val zone = ZoneId.systemDefault()
            val now = LocalDateTime.now(zone)
            val nextMidnight = LocalDateTime.of(LocalDate.now(zone).plusDays(1), LocalTime.MIDNIGHT)
            return Duration.between(now, nextMidnight)
        }
    }
}
