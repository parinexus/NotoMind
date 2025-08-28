package com.parinexus.selectlabelscreen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.state.ToggleableState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.parinexus.domain.repository.NoteTagRepository
import com.parinexus.domain.repository.NoteRepository
import com.parinexus.model.Label
import com.parinexus.model.NoteLabel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LabelViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val labelRepository: NoteTagRepository,
    private val notePadRepository: NoteRepository,
) : ViewModel() {

    var labelScreenUiState by mutableStateOf(LabelScreenUiState())

    private var list: List<LabelUiState> = emptyList()
    private var updateJob: Job? = null
    private var isMutating by mutableStateOf(false)

    private val labelsArgs = savedStateHandle.toRoute<LabelsArgs>()
    // فقط IDهای مثبت
    private val ids: Set<Long> = labelsArgs.ids
        .split(",")
        .mapNotNull { it.toLongOrNull() }
        .filter { it > 0L }
        .toSet()

    init {
        updateJob = viewModelScope.launch { updateList() }
    }

    fun onCheckClick(id: Long) {
        if (isMutating) return
        val labels = labelScreenUiState.labels.toMutableList()
        val index = labels.indexOfFirst { it.id == id }
        if (index < 0) return

        val current = labels[index]
        val toOn = current.toggleableState == ToggleableState.Off ||
                current.toggleableState == ToggleableState.Indeterminate

        // 1) جلوگیری از override شدن state توسط updateList در حال اجرا
        updateJob?.cancel()

        // 2) Optimistic UI
        val previous = current
        val optimistic = previous.copy(
            toggleableState = if (toOn) ToggleableState.On else ToggleableState.Off
        )
        labels[index] = optimistic
        labelScreenUiState = labelScreenUiState.copy(labels = labels.toImmutableList())

        // 3) DB و sync نهایی
        isMutating = true
        viewModelScope.launch {
            try {
                val targetIds = ids // در این صفحه، باید noteIdها معتبر باشند
                if (toOn) {
                    val pairings = targetIds.map { noteId -> NoteLabel(noteId = noteId, labelId = current.id) }
                    labelRepository.upsertNoteLabel(pairings)
                } else {
                    labelRepository.deleteNoteLabel(targetIds, current.id)
                }
            } catch (t: Throwable) {
                // اگر DB شکست خورد، UI را برگردان
                val rollback = labelScreenUiState.labels.toMutableList()
                val idx = rollback.indexOfFirst { it.id == previous.id }
                if (idx >= 0) {
                    rollback[idx] = previous
                    labelScreenUiState = labelScreenUiState.copy(labels = rollback.toImmutableList())
                }
            } finally {
                // پس از اتمام، دوباره از DB بخوان تا قطعی sync شود
                updateJob = viewModelScope.launch { updateList() }
                isMutating = false
            }
        }
    }

    fun onSearchChange(text: String) {
        if (text.isBlank()) {
            labelScreenUiState = labelScreenUiState.copy(editText = text)
            updateJob?.cancel()
            updateJob = viewModelScope.launch { updateList() }
        } else {
            val query = text.trim()
            val filtered = list.filter { it.label.contains(query, ignoreCase = true) }
            val haveSameText = list.any { it.label.equals(query, ignoreCase = true) }
            labelScreenUiState = labelScreenUiState.copy(
                editText = query,
                labels = filtered.toImmutableList(),
                showAddLabel = haveSameText.not(),
            )
        }
    }

    private suspend fun updateList() {
        // وضعیت تیک‌خوردن هر لیبل نسبت به چند نوت انتخاب‌شده
        val labelsCount: Map<Long, Int> = ids
            .flatMap { id ->
                val note = notePadRepository.getOneNotePad(id).firstOrNull()
                note?.labels ?: emptyList()
            }
            .groupingBy { it.id }
            .eachCount()

        val labels = (labelRepository.getAllLabels().firstOrNull().orEmpty()).map { label ->
            val state = when (labelsCount[label.id]) {
                ids.size -> ToggleableState.On
                null -> ToggleableState.Off
                else -> ToggleableState.Indeterminate
            }
            label.toLabelUiState().copy(toggleableState = state)
        }

        list = labels
        labelScreenUiState = labelScreenUiState.copy(
            showAddLabel = false,
            labels = labels.toImmutableList(),
            editText = "",
        )
    }

    fun onCreateLabel() {
        viewModelScope.launch {
            val name = labelScreenUiState.editText.trim()
            if (name.isEmpty()) return@launch

            // درج لیبل جدید
            labelRepository.upsert(listOf(Label(id = 0L, label = name)))

            // sync از DB
            updateJob?.cancel()
            updateJob = viewModelScope.launch { updateList() }

            // پیدا کردن id واقعی و فعال‌کردنش
            val createdId = list.firstOrNull { it.label.equals(name, ignoreCase = true) }?.id
            if (createdId != null) onCheckClick(createdId)
        }
    }
}
