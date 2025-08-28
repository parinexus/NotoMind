package com.parinexus.datastore

import androidx.datastore.core.DataStore
import app.cash.turbine.test
import com.parinexus.model.Contrast
import com.parinexus.model.DarkThemeConfig
import com.parinexus.model.ThemeBrand
import com.parinexus.model.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

private class FakeDataStore<T>(initial: T) : DataStore<T> {
    private val _state = MutableStateFlow(initial)
    override val data: Flow<T> = _state

    override suspend fun updateData(transform: suspend (t: T) -> T): T {
        val newVal = transform(_state.value)
        _state.value = newVal
        return newVal
    }
}

class UserPreferencesRepositoryTest {

    private lateinit var fakeStore: FakeDataStore<UserPreferences>
    private lateinit var repo: UserPreferencesRepository

    @Before
    fun setup() {
        fakeStore = FakeDataStore(defaultPrefs())
        repo = UserPreferencesRepository(fakeStore)
    }

    private fun defaultPrefs(): UserPreferences =
        UserPreferences.getDefaultInstance()

    private fun prefs(init: UserPreferences.Builder.() -> Unit): UserPreferences =
        UserPreferences.newBuilder().apply(init).build()

    @Test
    fun userData_maps_defaults() = runTest {
        fakeStore.updateData {
            it.toBuilder()
                .setThemeBrand(ThemeBrandProto.THEME_BRAND_UNSPECIFIED)
                .setDarkThemeConfig(DarkThemeConfigProto.DARK_THEME_CONFIG_UNSPECIFIED)
                .setUseDynamicColor(false)
                .setShouldHideOnboarding(false)
                .setContrast(ThemeContrastProto.THEME_CONTRAST_UNSPECIFIED)
                .build()
        }

        repo.userData.test {
            val item = awaitItem()
            assertEquals(
                UserData(
                    themeBrand = ThemeBrand.DEFAULT,            // falls back to DEFAULT
                    darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM, // falls back to FOLLOW_SYSTEM
                    useDynamicColor = false,
                    shouldHideOnboarding = false,
                    contrast = Contrast.Normal                   // falls back to Normal
                ),
                item
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun userData_maps_specific_values() = runTest {
        fakeStore.updateData {
            it.toBuilder()
                .setThemeBrand(ThemeBrandProto.THEME_BRAND_GREEN) // maps to ThemeBrand.PINK per repo
                .setDarkThemeConfig(DarkThemeConfigProto.DARK_THEME_CONFIG_DARK)
                .setUseDynamicColor(true)
                .setShouldHideOnboarding(true)
                .setContrast(ThemeContrastProto.THEME_CONTRAST_HIGH)
                .build()
        }

        repo.userData.test {
            val item = awaitItem()
            assertEquals(ThemeBrand.PINK, item.themeBrand)
            assertEquals(DarkThemeConfig.DARK, item.darkThemeConfig)
            assertEquals(true, item.useDynamicColor)
            assertEquals(true, item.shouldHideOnboarding)
            assertEquals(Contrast.High, item.contrast)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setThemeBrand_updates_store_and_flow() = runTest {
        repo.userData.test {
            skipItems(1)

            repo.setThemeBrand(ThemeBrand.PINK)

            val after = awaitItem()
            assertEquals(ThemeBrand.PINK, after.themeBrand)

            val currentPrefs = fakeStore.updateData { it } // no-op to fetch current
            assertEquals(ThemeBrandProto.THEME_BRAND_GREEN, currentPrefs.themeBrand)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setThemeContrast_updates_store_and_flow() = runTest {
        repo.userData.test {
            skipItems(1)

            repo.setThemeContrast(Contrast.Medium)
            val after = awaitItem()
            assertEquals(Contrast.Medium, after.contrast)

            val prefs = fakeStore.updateData { it }
            assertEquals(ThemeContrastProto.THEME_CONTRAST_MEDIUM, prefs.contrast)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setDynamicColorPreference_updates_store_and_flow() = runTest {
        repo.userData.test {
            skipItems(1)

            repo.setDynamicColorPreference(true)
            val after = awaitItem()
            assertEquals(true, after.useDynamicColor)

            val prefs = fakeStore.updateData { it }
            assertEquals(true, prefs.useDynamicColor)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setDarkThemeConfig_updates_store_and_flow() = runTest {
        repo.userData.test {
            skipItems(1)

            repo.setDarkThemeConfig(DarkThemeConfig.LIGHT)
            val after = awaitItem()
            assertEquals(DarkThemeConfig.LIGHT, after.darkThemeConfig)

            val prefs = fakeStore.updateData { it }
            assertEquals(DarkThemeConfigProto.DARK_THEME_CONFIG_LIGHT, prefs.darkThemeConfig)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setShouldHideOnboarding_updates_store_and_flow() = runTest {
        repo.userData.test {
            skipItems(1)

            repo.setShouldHideOnboarding(true)
            val after = awaitItem()
            assertEquals(true, after.shouldHideOnboarding)

            val prefs = fakeStore.updateData { it }
            assertEquals(true, prefs.shouldHideOnboarding)
            cancelAndIgnoreRemainingEvents()
        }
    }
}