package com.parinexus.domain.usecases

import com.parinexus.domain.repository.NoteRepository
import com.parinexus.model.NotoMind
import javax.inject.Inject

class CopyNoteUseCase @Inject constructor(
    private val repo: NoteRepository
) { suspend operator fun invoke(current: NotoMind) = repo.upsert(current.copy(id = -1)) }
