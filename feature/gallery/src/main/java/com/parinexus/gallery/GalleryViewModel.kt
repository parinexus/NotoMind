package com.parinexus.gallery

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.parinexus.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val notepadRepository: NoteRepository,
    private val imageToText: ImageToText,
) : ViewModel() {

    private val id = savedStateHandle.toRoute<GalleryArg>().id

    private val _galleryUiState = MutableStateFlow(GalleryUiState())
    val galleryUiState = _galleryUiState.asStateFlow()

    init {
        viewModelScope.launch {
            notepadRepository.getOneNotePad(id)
                .mapNotNull { it }
                .collectLatest { notoMind ->
                    _galleryUiState.value = GalleryUiState(
                        images = notoMind.images,
                    )
                }
        }
    }

    suspend fun onImage(path: String) {
        try {
            // val image = notePad.images[index]
            val text = try {
                imageToText.toText(path)
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
            var note = notepadRepository.getOneNotePad(id).first()!!
            note =
                note.copy(detail = "${note.detail}\n$text")
            notepadRepository.upsert(note)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteImage(id: Long) {
        viewModelScope.launch {
            notepadRepository.deleteImageNote(id)
        }
    }
}
