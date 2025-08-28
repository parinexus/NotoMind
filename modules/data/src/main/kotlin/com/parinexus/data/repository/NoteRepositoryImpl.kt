package com.parinexus.data.repository

import androidx.core.net.toUri
import com.parinexus.common.IContentManager
import com.parinexus.data.model.toNoteCheckEntity
import com.parinexus.data.model.toNoteEntity
import com.parinexus.data.model.toNoteImageEntity
import com.parinexus.data.model.toNotePad
import com.parinexus.database.dao.LabelDao
import com.parinexus.database.dao.NoteCheckDao
import com.parinexus.database.dao.NoteDao
import com.parinexus.database.dao.NoteImageDao
import com.parinexus.database.dao.NoteLabelDao
import com.parinexus.database.dao.NotepadDao
import com.parinexus.database.dao.PathDao
import com.parinexus.database.model.LabelEntity
import com.parinexus.database.model.NoteLabelEntity
import com.parinexus.domain.repository.NoteRepository
import com.parinexus.model.NotoMind
import com.parinexus.model.NoteType
import com.parinexus.model.NoteUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

internal class NoteRepositoryImpl
@Inject constructor(
    private val noteCheckDao: NoteCheckDao,
    private val noteDao: NoteDao,
    private val noteImageDao: NoteImageDao,
    private val noteLabelDao: NoteLabelDao,
    private val notePadDao: NotepadDao,
    private val labelDao: LabelDao,
    private val pathDao: PathDao,
    private val contentManager: IContentManager,
) : NoteRepository {

    override suspend fun upsert(notoMind: NotoMind): Long {
        val noteEntity = notoMind.copy(editDate = System.currentTimeMillis()).toNoteEntity()
        val rowId = noteDao.upsert(noteEntity)
        val noteId = if (noteEntity.id != 0L) noteEntity.id else rowId

        val checks = notoMind.checks.map { it.copy(noteId = noteId).toNoteCheckEntity() }
        if (checks.isNotEmpty()) noteCheckDao.upsert(checks)

        val images = notoMind.images.map { it.copy(noteId = noteId).toNoteImageEntity() }
        if (images.isNotEmpty()) noteImageDao.upsert(images)

        val labelIds = mutableListOf<Long>()
        for (label in notoMind.labels) {
            val name = label.label
            val id = labelDao.getIdByName(name) ?: labelDao.insert(LabelEntity(name = name))
            labelIds += id
        }

        noteLabelDao.clearNoteLabels(noteId)
        if (labelIds.isNotEmpty()) {
            val refs = labelIds.map { lid -> NoteLabelEntity(noteId = noteId, labelId = lid) }
            noteLabelDao.upsert(refs)
        }

        return noteId
    }

    override suspend fun upsert(notoMinds: List<NotoMind>) {
        for (n in notoMinds) upsert(n)
    }

    override suspend fun deleteCheckNote(id: Long, noteId: Long) = withContext(Dispatchers.IO) {
        noteCheckDao.delete(id, noteId)
    }

    override suspend fun deleteNoteCheckByNoteId(noteId: Long) = withContext(Dispatchers.IO) {
        noteCheckDao.deleteByNoteId(noteId)
    }

    override fun getNotePads(noteType: NoteType) = notePadDao
        .getListOfNotePad(noteType)
        .map { entities -> entities.map { transform(it.toNotePad()) } }

    override fun getNotePads() = notePadDao
        .getListOfNotePad()
        .map { entities -> entities.map { transform(it.toNotePad()) } }

    override fun getOneNotePad(id: Long): Flow<NotoMind?> {
        return notePadDao.getOneNotePad(id)
            .map { it?.toNotePad() }
            .map { pad -> pad?.let { transform(it) } }
    }

    override suspend fun deleteNotePad(notoMinds: List<NotoMind>) = withContext(Dispatchers.IO) {
        delete(notoMinds)
    }

    override suspend fun delete(notoMinds: List<NotoMind>) {
        for (it in notoMinds) {
            val id = it.id
            noteDao.delete(id)
            if (it.images.isNotEmpty()) noteImageDao.deleteByNoteId(id)
            if (it.labels.isNotEmpty()) noteLabelDao.clearNoteLabels(id)
            if (it.checks.isNotEmpty()) noteCheckDao.deleteByNoteId(id)
        }
        for (it in notoMinds) {
            pathDao.delete(it.id)
        }
    }

    override fun timeToString(time: LocalTime): String {
        val hour = when {
            time.hour > 12 -> time.hour - 12
            time.hour == 0 -> 12
            else -> time.hour
        }
        val timeset = if (time.hour > 11) "PM" else "AM"
        return "%2d : %02d %s".format(hour, time.minute, timeset)
    }

    override fun dateToString(date: LocalDate): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val month = date.month.name.lowercase().replaceFirstChar { it.uppercaseChar() }
        return when {
            now.date == date -> "Today"
            date == now.date.plus(1, DateTimeUnit.DAY) -> "Tomorrow"
            date.year != now.year -> "$month ${date.dayOfMonth}, ${date.year}"
            else -> "$month ${date.dayOfMonth}"
        }
    }

    override fun dateToString(long: Long): String {
        val date =
            Instant.fromEpochMilliseconds(long).toLocalDateTime(TimeZone.currentSystemDefault())
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val month =
            date.month.name.lowercase().replaceFirstChar { it.uppercaseChar() }.substring(0..2)
        return when {
            now.date == date.date -> "Today ${timeToString(date.time)} "
            date.date == now.date.plus(1, DateTimeUnit.DAY) -> "Tomorrow ${timeToString(date.time)}"
            date.year != now.year -> "$month ${date.dayOfMonth}, ${date.year} ${timeToString(date.time)}"
            else -> "$month ${date.dayOfMonth} ${timeToString(date.time)}"
        }
    }

    override fun saveImage(uri: String): Long = contentManager.saveImage(uri)

    override fun getUri(): String = contentManager.pictureUri()

    override fun getImagePath(id: Long): String = contentManager.getImagePath(id)

    override suspend fun deleteImageNote(id: Long) {
        noteImageDao.deleteById(id)
    }

    private fun transform(pad: NotoMind): NotoMind {
        return pad.copy(
            reminderString = dateToString(pad.reminder),
            editDateString = dateToString(pad.editDate),
            images = pad.images.map { it.copy(path = contentManager.getImagePath(it.id)) },
            uris = getUriFromDetail(pad.detail),
        )
    }

    private val regex =
        "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*)"

    private fun getUriFromDetail(detail: String): List<NoteUri> {
        return if (detail.contains(regex.toRegex())) {
            detail.split("\\s".toRegex())
                .filter { it.trim().matches(regex.toRegex()) }
                .mapIndexed { index, s ->
                    val host = s.toUri().authority ?: ""
                    val icon = "https://icon.horse/icon/$host"
                    NoteUri(
                        id = index,
                        icon = icon,
                        path = host,
                        uri = s,
                    )
                }
        } else emptyList()
    }
}