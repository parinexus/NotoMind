package com.parinexus.data.util

import com.parinexus.domain.ClockProvider
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class SystemClockProvider @Inject constructor() : ClockProvider {
    override fun nowLocal() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
}
