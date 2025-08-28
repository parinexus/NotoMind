package com.parinexus.detail

import android.annotation.SuppressLint
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.parinexus.detail.navigation.DetailArg
import com.parinexus.domain.usecases.AddCheckUseCase
import com.parinexus.domain.usecases.ArchiveNoteUseCase
import com.parinexus.domain.usecases.DeleteAlarmUseCase
import com.parinexus.domain.usecases.DeleteCheckUseCase
import com.parinexus.domain.usecases.DeleteCheckedUseCase
import com.parinexus.domain.usecases.GetPhotoUriUseCase
import com.parinexus.domain.usecases.HideChecksUseCase
import com.parinexus.domain.usecases.ObserveNoteUseCase
import com.parinexus.domain.usecases.SaveImageUseCase
import com.parinexus.domain.usecases.SetAlarmUseCase
import com.parinexus.domain.usecases.ToggleCheckUseCase
import com.parinexus.domain.usecases.UncheckAllUseCase
import com.parinexus.domain.usecases.UpdateCheckUseCase
import com.parinexus.domain.usecases.CopyNoteUseCase
import com.parinexus.domain.usecases.UpsertNoteUseCase
import com.parinexus.model.NotoMind
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeNote: ObserveNoteUseCase,
    private val upsertNote: UpsertNoteUseCase,
    private val copyNoteUseCase: CopyNoteUseCase,
    private val archiveNoteUseCase: ArchiveNoteUseCase,
    private val setAlarmUseCase: SetAlarmUseCase,
    private val deleteAlarmUseCase: DeleteAlarmUseCase,
    private val saveImageUseCase: SaveImageUseCase,
    private val getPhotoUriUseCase: GetPhotoUriUseCase,
    private val reminderCoordinator: ReminderCoordinator,
) : ViewModel() {


    private val _effects = MutableSharedFlow<DetailEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<DetailEffect> = _effects

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    private val noteId = savedStateHandle.toRoute<DetailArg>().id

    private var reminderUi: ReminderCoordinator.ReminderUi? = null

    init { processIntent(DetailIntent.LoadNote) }
    fun processIntent(intent: DetailIntent) {
        when (intent) {
            is DetailIntent.LoadNote -> loadNote()

            is DetailIntent.UpdateTitle -> updateNote { it.copy(title = intent.text) }
            is DetailIntent.UpdateContent -> updateNote { it.copy(detail = intent.text) }
            is DetailIntent.TogglePin -> updateNote { it.copy(isPin = !it.isPin) }
            is DetailIntent.ChangeColor -> updateNote { it.copy(color = intent.index) }
            is DetailIntent.ChangeImage -> updateNote { it.copy(background = intent.index) }
            is DetailIntent.AddCheck -> modifyChecks { AddCheckUseCase(upsertNote.repo)(it) }
            is DetailIntent.UpdateCheck -> modifyChecks { UpdateCheckUseCase(upsertNote.repo)(it, intent.id, intent.text) }
            is DetailIntent.ToggleCheck -> modifyChecks { ToggleCheckUseCase(upsertNote.repo)(it, intent.id, intent.isChecked) }
            is DetailIntent.DeleteCheck -> modifyChecks { DeleteCheckUseCase(upsertNote.repo)(it, intent.id) }
            is DetailIntent.UncheckAll -> modifyChecks { UncheckAllUseCase(upsertNote.repo)(it) }
            is DetailIntent.DeleteChecked -> modifyChecks { DeleteCheckedUseCase(upsertNote.repo)(it) }
            is DetailIntent.HideChecks -> modifyChecks { HideChecksUseCase(upsertNote.repo)(it) }

            is DetailIntent.ArchiveNote -> viewModelScope.launch {
                _state.value.note?.let { archiveNoteUseCase(it) }
            }
            is DetailIntent.DeleteNote -> viewModelScope.launch {
                _effects.emit(DetailEffect.CloseScreen)
            }
            is DetailIntent.CopyNote -> viewModelScope.launch {
                _state.value.note?.let { copyNoteUseCase(it) }
            }
            is DetailIntent.SaveNote -> saveNote()
            is DetailIntent.Exit -> onExit()

            is DetailIntent.SaveImage -> {
                val image = saveImageUseCase(intent.uri)
                _state.update { s ->
                    val n = s.note ?: NotoMind()
                    s.copy(note = n.copy(images = n.images + image))
                }
            }
            is DetailIntent.HideTime -> _state.update {
                it.copy(dateDialogUiData = it.dateDialogUiData.copy(showTimeDialog = false))
            }
            is DetailIntent.HideDate -> _state.update {
                it.copy(dateDialogUiData = it.dateDialogUiData.copy(showDateDialog = false))
            }
            is DetailIntent.ConfirmTime -> onConfirmTime()
            is DetailIntent.ConfirmDate -> onConfirmDate()
            is DetailIntent.SetTime -> onSetTimeIndex(intent.index)
            is DetailIntent.SetDate -> onSetDateIndex(intent.index)
            is DetailIntent.SetInterval -> onSetIntervalIndex(intent.index)

            is DetailIntent.SetAlarm -> {
                if (intent.time > 0L) setAlarm(intent.time, intent.interval) else setAlarmFromState()
            }
            is DetailIntent.DeleteAlarm -> deleteAlarm()
            else -> {}
        }
    }

    fun getPhotoUri(): String = getPhotoUriUseCase()

    private fun loadNote() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            observeNote(noteId).collectLatest { dbNote ->
                if (dbNote != null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = null,
                            note = dbNote,
                            title = TextFieldState(dbNote.title),
                            content = TextFieldState(dbNote.detail)
                        )
                    }
                    bindTextFieldsToNote()
                    initDate(dbNote)
                } else {
                    _state.update { it.copy(isLoading = false, error = "Note not found") }
                }
            }
        }
    }

    private fun bindTextFieldsToNote() {
        viewModelScope.launch {
            snapshotFlow { _state.value.title.text.toString() }
                .debounce(300)
                .distinctUntilChanged()
                .collect { text -> _state.update { s -> s.note?.let { n -> s.copy(note = n.copy(title = text)) } ?: s } }
        }
        viewModelScope.launch {
            snapshotFlow { _state.value.content.text.toString() }
                .debounce(300)
                .distinctUntilChanged()
                .collect { text -> _state.update { s -> s.note?.let { n -> s.copy(note = n.copy(detail = text)) } ?: s } }
        }
    }

    // ------------------------- Reminder glue ------------------------------
    private fun initDate(note: NotoMind) {
        reminderUi = reminderCoordinator.init(note)
        reminderUi?.let { ui ->
            _state.update {
                it.copy(
                    dateDialogUiData = ui.dd,
                    datePicker = ui.datePicker,
                    timePicker = ui.timePicker
                )
            }
        }
    }
    private fun onSetDateIndex(index: Int) {
        reminderUi = reminderUi?.let { reminderCoordinator.onSetDateIndex(it, index) }
        reminderUi?.let { ui -> _state.update { it.copy(dateDialogUiData = ui.dd, datePicker = ui.datePicker) } }
    }

    private fun onSetTimeIndex(index: Int) {
        reminderUi = reminderUi?.let { reminderCoordinator.onSetTimeIndex(it, index) }
        reminderUi?.let { ui -> _state.update { it.copy(dateDialogUiData = ui.dd, timePicker = ui.timePicker) } }
    }

    private fun onSetIntervalIndex(index: Int) {
        reminderUi = reminderUi?.let { reminderCoordinator.onSetIntervalIndex(it, index) }
        reminderUi?.let { ui -> _state.update { it.copy(dateDialogUiData = ui.dd) } }
    }


    private fun onConfirmDate() {
        val millis = _state.value.datePicker.selectedDateMillis
        reminderUi = reminderUi?.let { reminderCoordinator.onConfirmDate(it, millis) }
        reminderUi?.let { ui -> _state.update { it.copy(dateDialogUiData = ui.dd) } }
    }

    private fun onConfirmTime() {
        val hour = _state.value.timePicker.hour
        val minute = _state.value.timePicker.minute
        reminderUi = reminderUi?.let { reminderCoordinator.onConfirmTime(it, hour, minute) }
        reminderUi?.let { ui -> _state.update { it.copy(dateDialogUiData = ui.dd) } }
    }

    private fun setAlarmFromState() {
        val ui = reminderUi ?: run {
            Timber.w("Reminder UI is null; cannot set alarm from state")
            return
        }
        val pair = reminderCoordinator.buildAlarmFromState(ui)
        if (pair == null) {
            Timber.w("Alarm not set: selected time is in the past")
            _state.update { it.copy(dateDialogUiData = it.dateDialogUiData.copy(timeError = true)) }
        } else setAlarm(pair.first, pair.second)
    }

    private fun setAlarm(time: Long, interval: Long?) {
        viewModelScope.launch {
            val current = _state.value.note ?: return@launch
            val updated = setAlarmUseCase(current, time, interval)
            _state.update { it.copy(note = updated) }
        }
    }
    private fun deleteAlarm() {
        viewModelScope.launch {
            val current = _state.value.note ?: return@launch
            deleteAlarmUseCase(current)
            _state.update { it.copy(note = current.copy(reminder = -1, interval = -1, reminderString = "")) }
        }
    }

    // ------------------------- Note ops ----------------------------------
    private fun updateNote(transform: (NotoMind) -> NotoMind) {
        val current = _state.value.note ?: return
        val updated = transform(current)
        _state.update { it.copy(note = updated) }
        viewModelScope.launch { upsertNote(updated) }
    }

    private fun modifyChecks(block: suspend (NotoMind) -> Unit) {
        viewModelScope.launch { _state.value.note?.let { block(it) } }
    }

    private fun saveNote() {
        viewModelScope.launch {
            val s = _state.value
            val n = s.note ?: return@launch
            val finalNote = n.copy(
                title = s.title.text.toString(),
                detail = s.content.text.toString()
            )
            _state.update { it.copy(note = finalNote) }
            upsertNote(finalNote)
            _effects.emit(DetailEffect.CloseScreen)
        }
    }


    private fun onExit() {
        viewModelScope.launch {
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun getAudioLength(path: String): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(path)
            val t = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            t?.toLong() ?: 1L
        } finally { retriever.release() }
    }
}
