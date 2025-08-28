package com.parinexus.domain

interface ClockProvider {
    fun nowLocal(): kotlinx.datetime.LocalDateTime
}
