package com.parinexus.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.parinexus.domain.repository.UserSettingsRepository
import com.parinexus.model.DarkThemeConfig
import com.parinexus.model.ThemeBrand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository,
) : ViewModel() {

    private val _settingState = MutableStateFlow<SettingState>(SettingState.Loading())
    val settingState = _settingState.asStateFlow()

    init {
        update()
    }

    fun setThemeBrand(themeBrand: ThemeBrand) {
        viewModelScope.launch {
            _settingState.value = SettingState.Loading()

            userSettingsRepository.setThemeBrand(themeBrand)

            update()
        }
    }

    fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        viewModelScope.launch {
            _settingState.value = SettingState.Loading()

            userSettingsRepository.setDarkThemeConfig(darkThemeConfig)

            update()
        }
    }

    private fun update() {
        viewModelScope.launch {
            _settingState.value = userSettingsRepository.userData.map {
                SettingState.Success(
                    themeBrand = it.themeBrand,
                    darkThemeConfig = it.darkThemeConfig,
                )
            }.first()
        }
    }
}
