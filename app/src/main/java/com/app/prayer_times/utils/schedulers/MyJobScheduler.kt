package com.app.prayer_times.utils.schedulers

import android.app.job.JobScheduler
import android.content.Context

class MyJobScheduler(private val context: Context) {
    val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
}