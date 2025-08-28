package com.parinexus.notomind

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parinexus.domain.repository.NoteTagRepository
import com.parinexus.domain.repository.NoteRepository
import com.parinexus.domain.repository.UserSettingsRepository
import com.parinexus.model.NoteCheck
import com.parinexus.model.NoteImage
import com.parinexus.model.NotoMind
import com.parinexus.model.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userSettingsRepository: UserSettingsRepository,
    private val notePadRepository: NoteRepository,
    private val labelRepository: NoteTagRepository,

    ) : ViewModel() {
    val uiState: StateFlow<MainActivityUiState> = userSettingsRepository
        .userData.map {
            MainActivityUiState.Success(it)
        }.stateIn(
            scope = viewModelScope,
            initialValue = MainActivityUiState.Loading,
            started = SharingStarted.WhileSubscribed(5_000),
        )

    val labels = labelRepository
        .getAllLabels().stateIn(
            scope = viewModelScope,
            initialValue = emptyList(),
            started = SharingStarted.WhileSubscribed(5_000),
        )

    suspend fun insertNewNote(): Long {
        return notePadRepository.upsert(NotoMind())
    }

    suspend fun insertNewImageNote(uri: String): Long {
        val id = notePadRepository.saveImage(uri)

        val image = NoteImage(
            id = id,
        )

        val notoMind = NotoMind(
            images = listOf(image),
        )
        return notePadRepository.upsert(notoMind)
    }
    suspend fun insertNewDrawing(): Pair<Long, Long> {
        val drawing = NoteImage(
            id = System.currentTimeMillis(),
        )

        val notoMind = NotoMind(
            images = listOf(drawing),
        )

        val noteId = notePadRepository.upsert(notoMind)

        return Pair(noteId, drawing.id)
    }
    suspend fun insertNewCheckNote(): Long {
        val notoMind = NotoMind(
            isCheck = true,
            checks = listOf(NoteCheck()),
        )
        return notePadRepository.upsert(notoMind)
    }

    fun pictureUri(): String {
        return notePadRepository.getUri()
    }

    suspend fun newSharePost(title: String, subject: String, images: List<String>): Long {
        println("images $images, title $title, subject $subject")
        val noteImage = images
            .map { notePadRepository.saveImage(it) }
            .map { NoteImage(id = it) }

        val notoMind = NotoMind(
            title = title,
            detail = subject,
            images = noteImage,
        )
        return notePadRepository.upsert(notoMind)
    }
}

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Success(val userData: UserData) : MainActivityUiState
}
