package com.parinexus.domain.usecases

import com.parinexus.model.NotoMind
import com.parinexus.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class ObserveNoteUseCase @Inject constructor(
    private val repo: NoteRepository
) {
    operator fun invoke(noteId: Long): Flow<NotoMind?> =
        if (noteId == -1L) flowOf(NotoMind()) else repo.getOneNotePad(noteId)
}