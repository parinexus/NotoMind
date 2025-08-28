package com.parinexus.model

data class NoteImage(
    val id: Long = -1,
    val noteId: Long = 0,
    val path: String = "",
    val timestamp: Long = 0,
)
