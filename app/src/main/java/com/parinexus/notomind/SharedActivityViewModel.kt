package com.parinexus.notomind

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parinexus.domain.repository.NoteTagRepository
import com.parinexus.domain.repository.NoteRepository
import com.parinexus.model.Contrast
import com.parinexus.model.DarkThemeConfig
import com.parinexus.model.Label
import com.parinexus.model.NoteImage
import com.parinexus.model.NotoMind
import com.parinexus.model.ThemeBrand
import com.parinexus.model.UserData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SharedActivityViewModel @Inject constructor(
    private val notePadRepository: NoteRepository,
    private val labelRepository: NoteTagRepository,

    ) : ViewModel() {

    private val _state = MutableStateFlow<SharedActivityUiState>(SharedActivityUiState.Loading)
    val state = _state.asStateFlow()

    val title = TextFieldState()
    val content = TextFieldState()

    init {
        viewModelScope.launch {
            launch {
                snapshotFlow { content.text }
                    .debounce(500)
                    .collectLatest { text ->
                        _state.update {
                            val success = it

                            if (success is SharedActivityUiState.Success) {
                                success.copy(notoMind = success.notoMind.copy(detail = text.toString()))
                            } else {
                                it
                            }
                        }
                        save()
                    }
            }
            launch {
                snapshotFlow { title.text }
                    .debounce(500)
                    .collectLatest { text ->
                        _state.update {
                            val success = it

                            if (success is SharedActivityUiState.Success) {
                                success.copy(notoMind = success.notoMind.copy(title = text.toString()))
                            } else {
                                it
                            }
                        }
                        save()
                    }
            }
        }
    }

    private fun save() {
        viewModelScope.launch {
            val st = state.value
            if (st is SharedActivityUiState.Success) {
                notePadRepository.upsert(st.notoMind)
            }
        }
    }

    fun toggleCheck(index: Int) {
        val success = state.value as SharedActivityUiState.Success
        val label = success.labels[index]
        val labels = success.notoMind.labels.toMutableList()

        if (labels.contains(label)) {
            labels.remove(label)
        } else {
            labels.add(label)
        }
        _state.update {
            success.copy(notoMind = success.notoMind.copy(labels = labels))
        }
        save()
    }

    fun newSharePost(title1: String, subject2: String, images: List<String>) {
        viewModelScope.launch {
            println("images $images, title $title1, subject $subject2")

            val labels = async {
                labelRepository.getAllLabels().first()
            }

            title.edit {
                this.append(title1)
            }
            content.edit {
                append(subject2)
            }

            val noteImage = images
                .map { notePadRepository.saveImage(it) }
                .map { NoteImage(id = it) }

            val notoMind = NotoMind(
                title = title1,
                detail = subject2,
                images = noteImage,
            )
            val id = notePadRepository.upsert(notoMind)

            val newNote = notePadRepository.getOneNotePad(id).first()!!
            _state.update {
                SharedActivityUiState.Success(
                    labels = labels.await(),
                    notoMind = newNote,
                )
            }
        }
    }

    suspend fun delete() {
        val success = state.value as SharedActivityUiState.Success
        notePadRepository.delete(listOf(success.notoMind))
    }
}

sealed interface SharedActivityUiState {
    data object Loading : SharedActivityUiState
    data class Success(
        val userData: UserData = UserData(
            themeBrand = ThemeBrand.DEFAULT,
            darkThemeConfig = DarkThemeConfig.LIGHT,
            useDynamicColor = false,
            shouldHideOnboarding = false,
            contrast = Contrast.High,
        ),
        val notoMind: NotoMind = NotoMind(),
        val labels: List<Label> = emptyList(),
    ) : SharedActivityUiState
}
