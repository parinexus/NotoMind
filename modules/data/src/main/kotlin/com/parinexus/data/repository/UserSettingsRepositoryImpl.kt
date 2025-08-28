package com.parinexus.data.repository

import com.parinexus.analytics.AnalyticsHelper
import com.parinexus.datastore.UserPreferencesRepository
import com.parinexus.domain.repository.UserSettingsRepository
import com.parinexus.model.Contrast
import com.parinexus.model.DarkThemeConfig
import com.parinexus.model.ThemeBrand
import com.parinexus.model.UserData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

internal class UserSettingsRepositoryImpl @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val analyticsHelper: AnalyticsHelper,
) : UserSettingsRepository {

    override val userData: Flow<UserData> =
        userPreferencesRepository.userData

    override suspend fun setThemeBrand(themeBrand: ThemeBrand) {
        userPreferencesRepository.setThemeBrand(themeBrand)
        analyticsHelper.logThemeChanged(themeBrand.name)
    }

    override suspend fun setThemeContrast(contrast: Contrast) {
        userPreferencesRepository.setThemeContrast(contrast)
        analyticsHelper.logContrastChanged(contrast.name)
    }

    override suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        userPreferencesRepository.setDarkThemeConfig(darkThemeConfig)
        analyticsHelper.logDarkThemeConfigChanged(darkThemeConfig.name)
    }

    override suspend fun setDynamicColorPreference(useDynamicColor: Boolean) {
        userPreferencesRepository.setDynamicColorPreference(useDynamicColor)
        analyticsHelper.logDynamicColorPreferenceChanged(useDynamicColor)
    }

    override suspend fun setShouldHideOnboarding(shouldHideOnboarding: Boolean) {
        userPreferencesRepository.setShouldHideOnboarding(shouldHideOnboarding)
        analyticsHelper.logOnboardingStateChanged(shouldHideOnboarding)
    }
}
