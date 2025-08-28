package com.parinexus.domain.repository

import com.parinexus.model.Contrast
import com.parinexus.model.DarkThemeConfig
import com.parinexus.model.ThemeBrand
import com.parinexus.model.UserData
import kotlinx.coroutines.flow.Flow

interface UserSettingsRepository {

    val userData: Flow<UserData>

    suspend fun setThemeBrand(themeBrand: ThemeBrand)

    suspend fun setThemeContrast(contrast: Contrast)

    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig)

    suspend fun setDynamicColorPreference(useDynamicColor: Boolean)

    suspend fun setShouldHideOnboarding(shouldHideOnboarding: Boolean)
}
