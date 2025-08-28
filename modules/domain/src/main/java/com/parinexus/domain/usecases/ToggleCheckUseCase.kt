package com.parinexus.domain.usecases

import com.parinexus.domain.repository.NoteRepository
import com.parinexus.model.NotoMind
import javax.inject.Inject

class ToggleCheckUseCase @Inject constructor(private val repo: NoteRepository) {
    suspend operator fun invoke(note: NotoMind, id: Long, isChecked: Boolean) =
        repo.upsert(note.copy(checks = note.checks.map { c -> if (c.id == id) c.copy(isCheck = isChecked) else c }))
}