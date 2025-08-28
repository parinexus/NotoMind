package com.parinexus.domain.usecases

import com.parinexus.domain.repository.NoteRepository
import com.parinexus.model.NotoMind
import javax.inject.Inject

class UncheckAllUseCase @Inject constructor(private val repo: NoteRepository) {
    suspend operator fun invoke(note: NotoMind) = repo.upsert(note.copy(checks = note.checks.map { it.copy(isCheck = false) }))
}