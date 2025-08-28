package com.parinexus.analytics

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "StubAnalyticsLogger"

@Singleton
internal class StubAnalyticsLogger @Inject constructor() : AnalyticsLogger {
    override fun logEvent(event: TrackingEvent) {
        Log.d(TAG, "Received analytics event: $event")
    }
}
