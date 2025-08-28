package com.parinexus.analytics

class NoOpAnalyticsLogger : AnalyticsLogger {
    override fun logEvent(event: TrackingEvent) = Unit
}
