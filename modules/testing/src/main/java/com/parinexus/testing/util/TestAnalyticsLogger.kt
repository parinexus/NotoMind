package com.parinexus.testing.util

import com.parinexus.analytics.TrackingEvent
import com.parinexus.analytics.AnalyticsLogger

class TestAnalyticsLogger : AnalyticsLogger {

    private val events = mutableListOf<TrackingEvent>()
    override fun logEvent(event: TrackingEvent) {
        events.add(event)
    }

    fun hasLogged(event: TrackingEvent) = event in events
}
