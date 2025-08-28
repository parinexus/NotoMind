package com.parinexus.data.repository

import com.parinexus.analytics.AnalyticsLogger
import com.parinexus.analytics.TrackingEvent

internal fun AnalyticsLogger.logNewsResourceBookmarkToggled(
    newsResourceId: String,
    isBookmarked: Boolean,
) {
    val eventType = if (isBookmarked) "news_resource_saved" else "news_resource_unsaved"
    val paramKey = if (isBookmarked) "saved_news_resource_id" else "unsaved_news_resource_id"
    logEvent(
        TrackingEvent(
            type = eventType,
            extras = listOf(
                TrackingEvent.Param(key = paramKey, value = newsResourceId),
            ),
        ),
    )
}

internal fun AnalyticsLogger.logTopicFollowToggled(followedTopicId: String, isFollowed: Boolean) {
    val eventType = if (isFollowed) "topic_followed" else "topic_unfollowed"
    val paramKey = if (isFollowed) "followed_topic_id" else "unfollowed_topic_id"
    logEvent(
        TrackingEvent(
            type = eventType,
            extras = listOf(
                TrackingEvent.Param(key = paramKey, value = followedTopicId),
            ),
        ),
    )
}

internal fun AnalyticsLogger.logThemeChanged(themeName: String) =
    logEvent(
        TrackingEvent(
            type = "theme_changed",
            extras = listOf(
                TrackingEvent.Param(key = "theme_name", value = themeName),
            ),
        ),
    )

internal fun AnalyticsLogger.logContrastChanged(contrastName: String) =
    logEvent(
        TrackingEvent(
            type = "Contrast_changed",
            extras = listOf(
                TrackingEvent.Param(key = "theme_name", value = contrastName),
            ),
        ),
    )

internal fun AnalyticsLogger.logDarkThemeConfigChanged(darkThemeConfigName: String) =
    logEvent(
        TrackingEvent(
            type = "dark_theme_config_changed",
            extras = listOf(
                TrackingEvent.Param(key = "dark_theme_config", value = darkThemeConfigName),
            ),
        ),
    )

internal fun AnalyticsLogger.logDynamicColorPreferenceChanged(useDynamicColor: Boolean) =
    logEvent(
        TrackingEvent(
            type = "dynamic_color_preference_changed",
            extras = listOf(
                TrackingEvent.Param(
                    key = "dynamic_color_preference",
                    value = useDynamicColor.toString(),
                ),
            ),
        ),
    )

internal fun AnalyticsLogger.logOnboardingStateChanged(shouldHideOnboarding: Boolean) {
    val eventType = if (shouldHideOnboarding) "onboarding_complete" else "onboarding_reset"
    logEvent(
        TrackingEvent(type = eventType),
    )
}
