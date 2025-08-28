package com.parinexus.analytics

import javax.inject.Inject

/**
 * Implementation of `AnalyticsLogger` which logs events to a Firebase backend.
 */
internal class FirebaseAnalyticsLogger @Inject constructor() : AnalyticsLogger {

    override fun logEvent(event: TrackingEvent) {
    }
}
