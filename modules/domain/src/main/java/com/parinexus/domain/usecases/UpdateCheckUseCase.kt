package com.parinexus.domain.usecases

import com.parinexus.domain.repository.NoteRepository
import com.parinexus.model.NotoMind
import javax.inject.Inject

class UpdateCheckUseCase @Inject constructor(private val repo: NoteRepository) {
    suspend operator fun invoke(note: NotoMind, id: Long, text: String) =
        repo.upsert(note.copy(checks = note.checks.map { c -> if (c.id == id) c.copy(content = text) else c }))
}