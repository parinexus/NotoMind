package com.parinexus.domain.usecases

import com.parinexus.domain.repository.NoteRepository
import javax.inject.Inject

class GetPhotoUriUseCase @Inject constructor(
    private val repo: NoteRepository
) { operator fun invoke(): String = repo.getUri() }
