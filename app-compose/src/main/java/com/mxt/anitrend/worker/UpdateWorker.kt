package com.mxt.anitrend.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class UpdateWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d("UpdateWorker", "Update check: no new version")
        return Result.success()
    }
}
