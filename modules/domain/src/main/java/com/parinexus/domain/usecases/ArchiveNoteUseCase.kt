package com.parinexus.domain.usecases

import com.parinexus.domain.repository.NoteRepository
import com.parinexus.model.NoteType
import com.parinexus.model.NotoMind
import javax.inject.Inject

class ArchiveNoteUseCase @Inject constructor(
    private val repo: NoteRepository
) { suspend operator fun invoke(note: NotoMind) =
    repo.upsert(note.copy(noteType = if (note.noteType == NoteType.ARCHIVE) NoteType.NOTE else NoteType.ARCHIVE)) }
