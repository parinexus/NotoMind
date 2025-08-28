package com.parinexus.domain.reminder

const val MS_PER_DAY: Long = 24L * 60L * 60L * 1000L

enum class RepeatInterval(val millis: Long?) {
    NONE(null),
    DAILY(MS_PER_DAY),
    WEEKLY(7 * MS_PER_DAY),
    MONTHLY(30 * MS_PER_DAY),
    YEARLY(365 * MS_PER_DAY)
}

fun intervalIndex(interval: Long): Int = when (interval) {
    MS_PER_DAY -> 1
    7 * MS_PER_DAY -> 2
    30 * MS_PER_DAY -> 3
    365 * MS_PER_DAY -> 4
    else -> 0
}

fun indexToInterval(index: Int): RepeatInterval = when (index) {
    1 -> RepeatInterval.DAILY
    2 -> RepeatInterval.WEEKLY
    3 -> RepeatInterval.MONTHLY
    4 -> RepeatInterval.YEARLY
    else -> RepeatInterval.NONE
}