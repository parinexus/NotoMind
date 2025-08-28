package com.parinexus.analytics

interface AnalyticsLogger {
    fun logEvent(event: TrackingEvent)
}
