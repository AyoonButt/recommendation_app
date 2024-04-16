package com.example.firedatabase_assis

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat

object JobScheduler {

    private const val JOB_ID = 123 // Unique ID for the job
    private const val JOB_INTERVAL = 24 * 60 * 60 * 1000L // Interval in milliseconds (24 hours)
    private const val JOB_FLEXIBLE_INTERVAL =
        5 * 60 * 1000L // Flexible interval in milliseconds (5 minutes)

    fun scheduleJob(context: Context) {
        val componentName = ComponentName(context, StreamingAPI::class.java)
        val builder = JobInfo.Builder(JOB_ID, componentName)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPersisted(true) // Required for periodic jobs to persist across reboots

        // Calculate the time when you want the job to start
        val startTimeMillis = System.currentTimeMillis() + JOB_INTERVAL

        // Set the minimum latency to start the job
        builder.setMinimumLatency(startTimeMillis)

        // Set the maximum delay for the job to be executed
        builder.setOverrideDeadline(startTimeMillis + JOB_FLEXIBLE_INTERVAL)

        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(builder.build())
    }


    fun cancelJob(context: Context) {
        val jobScheduler = ContextCompat.getSystemService(context, JobScheduler::class.java)
        jobScheduler?.cancel(JOB_ID)
    }
}
