package com.parinexus.analytics

data class TrackingEvent(
    val type: String,
    val extras: List<Param> = emptyList(),
) {
    class Types {
        companion object Companion {
            const val SCREEN_VIEW = "screen_view"
        }
    }

    data class Param(val key: String, val value: String)

    class ParamKeys {
        companion object Companion {
            const val SCREEN_NAME = "screen_name"
        }
    }
}
