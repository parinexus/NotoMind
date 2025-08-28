package com.parinexus.analytics

import androidx.compose.runtime.staticCompositionLocalOf

val LocalAnalyticsLogger = staticCompositionLocalOf<AnalyticsLogger> {
    NoOpAnalyticsLogger()
}
