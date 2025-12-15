package com.attendifyplus

import android.app.Application
import androidx.work.*
import com.attendifyplus.di.appModule
import com.attendifyplus.sync.SyncWorker
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber
import java.util.concurrent.TimeUnit

class AttendifyPlusApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialize Koin for Dependency Injection
        startKoin {
            androidContext(this@AttendifyPlusApplication)
            modules(appModule)
        }

        // Schedule the periodic sync worker
        setupRecurringSync()
    }

    private fun setupRecurringSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val repeatingRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            1, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "background_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            repeatingRequest
        )

        Timber.d("Hourly background sync scheduled.")
    }
}
