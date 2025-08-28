package com.parinexus.domain.usecases

import com.parinexus.domain.repository.NoteRepository
import com.parinexus.model.NoteImage
import javax.inject.Inject

class SaveImageUseCase @Inject constructor(
    private val repo: NoteRepository
) {
    operator fun invoke(uri: String): NoteImage {
        val id = repo.saveImage(uri)
        return NoteImage(id = id, path = repo.getImagePath(id))
    }
}
