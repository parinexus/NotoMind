package com.parinexus.notomind

import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class NotePadApplication : android.app.Application() {
    override fun onCreate() {
        super.onCreate()
        // Saver.initialize(applicationContext)

        if (packageName.contains("debug")) {
            Timber.plant(Timber.DebugTree())
            Timber.e("log on app create")
        }
    }
}
