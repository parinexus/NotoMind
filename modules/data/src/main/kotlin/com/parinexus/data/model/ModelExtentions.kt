package com.parinexus.data.model

import com.parinexus.database.model.FullLabel
import com.parinexus.database.model.LabelEntity
import com.parinexus.database.model.NoteCheckEntity
import com.parinexus.database.model.NoteEntity
import com.parinexus.database.model.NoteImageEntity
import com.parinexus.database.model.NoteLabelEntity
import com.parinexus.database.model.NotoMindEntity
import com.parinexus.model.Label
import com.parinexus.model.Note
import com.parinexus.model.NoteCheck
import com.parinexus.model.NoteImage
import com.parinexus.model.NoteLabel
import com.parinexus.model.NotoMind

private fun Long?.sanitizeId(): Long = when (this) {
    null, -1L -> 0L
    else -> this
}

fun LabelEntity.toLabel(): Label = Label(
    id = id,
    label = name
)

fun Label.toLabelEntity(): LabelEntity = LabelEntity(
    id = id.sanitizeId(),
    name = label
)

fun NoteCheckEntity.toNoteCheck(): NoteCheck = NoteCheck(
    id = id,
    noteId = noteId,
    content = content,
    isCheck = isCheck
)

fun NoteCheck.toNoteCheckEntity(): NoteCheckEntity = NoteCheckEntity(
    id = id.sanitizeId(),
    noteId = noteId,
    content = content,
    isCheck = isCheck
)

fun NotoMind.toNoteEntity(): NoteEntity = NoteEntity(
    id = id.sanitizeId(),
    title = title,
    detail = detail,
    editDate = editDate,
    isCheck = isCheck,
    color = color,
    background = background,
    isPin = isPin,
    reminder = reminder,
    interval = interval,
    noteType = noteType
)

fun NoteEntity.toNote(): Note = Note(
    id = id,
    title = title,
    detail = detail,
    editDate = editDate,
    isCheck = isCheck,
    color = color,
    background = background,
    isPin = isPin,
    reminder = reminder,
    interval = interval,
    noteType = noteType
)

fun NoteImage.toNoteImageEntity(): NoteImageEntity = NoteImageEntity(
    id = id.sanitizeId(),
    noteId = noteId,
    timestamp = timestamp
)

fun NoteImageEntity.toNoteImage(): NoteImage = NoteImage(
    id = id,
    noteId = noteId,
    timestamp = timestamp
)

fun NoteLabelEntity.toNoteLabel(): NoteLabel = NoteLabel(
    noteId = noteId,
    labelId = labelId
)

fun NoteLabel.toNoteLabelEntity(): NoteLabelEntity = NoteLabelEntity(
    noteId = noteId,
    labelId = labelId
)

fun NotoMindEntity.toNotePad(): NotoMind = NotoMind(
    id = noteEntity.id,
    title = noteEntity.title,
    detail = noteEntity.detail,
    editDate = noteEntity.editDate,
    isCheck = noteEntity.isCheck,
    color = noteEntity.color,
    background = noteEntity.background,
    isPin = noteEntity.isPin,
    reminder = noteEntity.reminder,
    interval = noteEntity.interval,
    noteType = noteEntity.noteType,
    images = images.map { it.toNoteImage() },
    checks = checks.map { it.toNoteCheck() },
    labels = labels.map { it.toLabel() }
)

fun FullLabel.toLabel(): Label = label.toLabel()
