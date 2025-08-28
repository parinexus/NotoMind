package com.parinexus.model

enum class NoteType(val index: Long = 0) {
    NOTE(-1),
    ARCHIVE(-2),
    LABEL,
    REMAINDER(-4),
}
