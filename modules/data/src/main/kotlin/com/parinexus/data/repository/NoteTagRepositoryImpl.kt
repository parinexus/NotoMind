package com.parinexus.data.repository

import com.parinexus.data.model.toLabel
import com.parinexus.data.model.toLabelEntity
import com.parinexus.data.model.toNoteLabel
import com.parinexus.data.model.toNoteLabelEntity
import com.parinexus.database.dao.LabelDao
import com.parinexus.database.dao.NoteLabelDao
import com.parinexus.domain.repository.NoteTagRepository
import com.parinexus.model.Label
import com.parinexus.model.NoteLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class NoteTagRepositoryImpl
@Inject constructor(
    private val labelDao: LabelDao,
    private val noteLabelDao: NoteLabelDao,
) : NoteTagRepository {

    override suspend fun upsert(labels: List<Label>) = withContext(Dispatchers.IO) {
        labelDao.upsert(labels.map { it.toLabelEntity() })
    }

    override suspend fun upsertNoteLabel(notelabels: List<NoteLabel>) {
        noteLabelDao.upsert(notelabels.map { it.toNoteLabelEntity() })
    }

    override fun getNoteLabel(id: Long): Flow<List<NoteLabel>> {
        return noteLabelDao.getAll(id).map { it.map { it.toNoteLabel() } }
    }

    override suspend fun deleteNoteLabel(noteIds: Set<Long>, labelId: Long) {
        noteLabelDao.delete(noteIds, labelId)
    }

    override suspend fun getOneLabelList(): List<Label> {
        return labelDao.getAllLabelsOneShot().map { it.toLabel() }
    }

    override fun getAllLabels() =
        labelDao.getAllLabels().map { labelEntities -> labelEntities.map { it.toLabel() } }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        labelDao.delete(id)
        noteLabelDao.clearNoteLabels(id)
    }
}
