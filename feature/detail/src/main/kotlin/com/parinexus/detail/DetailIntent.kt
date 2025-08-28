package com.parinexus.detail

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import com.parinexus.model.NotoMind
import com.parinexus.ui.state.DateDialogUiData
import java.util.Locale

sealed interface DetailIntent {
    data object LoadNote : DetailIntent
    data object SaveNote : DetailIntent
    data class UpdateTitle(val text: String) : DetailIntent
    data class UpdateContent(val text: String) : DetailIntent
    data class TogglePin(val id: Long?) : DetailIntent
    data class ChangeColor(val index: Int) : DetailIntent
    data class ChangeImage(val index: Int) : DetailIntent
    data class AddCheck(val noteId: Long?) : DetailIntent
    data class UpdateCheck(val id: Long, val text: String) : DetailIntent
    data class ToggleCheck(val id: Long, val isChecked: Boolean) : DetailIntent
    data class DeleteCheck(val id: Long) : DetailIntent
    data object UncheckAll : DetailIntent
    data object DeleteChecked : DetailIntent
    data object HideChecks : DetailIntent
    data object ArchiveNote : DetailIntent
    data object DeleteNote : DetailIntent
    data object CopyNote : DetailIntent
    data class SetAlarm(val time: Long, val interval: Long?) : DetailIntent
    data object DeleteAlarm : DetailIntent
    data object Exit : DetailIntent
    data class SaveImage(val uri: String) : DetailIntent
    object GetPhotoUri : DetailIntent

    data class SetTime(val index: Int) : DetailIntent
    data class SetDate(val index: Int) : DetailIntent
    data class SetInterval(val index: Int) : DetailIntent

    object ConfirmTime : DetailIntent
    object ConfirmDate : DetailIntent
    object HideTime : DetailIntent
    object HideDate : DetailIntent
}

sealed interface DetailState {
    data object Loading : DetailState
    data class Success(val note: NotoMind) : DetailState
    data class Error(val message: String) : DetailState
}

@OptIn(ExperimentalMaterial3Api::class)
data class DetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val note: NotoMind? = null,

    val title: TextFieldState = TextFieldState(),
    val content: TextFieldState = TextFieldState(),

    val dateDialogUiData: DateDialogUiData = DateDialogUiData(),
    val timePicker: TimePickerState = TimePickerState(12, 0, false),
    val datePicker: DatePickerState = DatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis(),
        locale = Locale.getDefault()
    )
)

sealed interface DetailEffect {
    data object CloseScreen : DetailEffect
}
