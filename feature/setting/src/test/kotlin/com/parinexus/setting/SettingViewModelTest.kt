package com.parinexus.setting

import com.parinexus.domain.repository.UserSettingsRepository
import com.parinexus.model.Contrast
import com.parinexus.model.DarkThemeConfig
import com.parinexus.model.ThemeBrand
import com.parinexus.model.UserData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private lateinit var repo: UserSettingsRepository
    private lateinit var vm: SettingViewModel
    private lateinit var userDataFlow: MutableStateFlow<UserData>

    private val initial = UserData(
        themeBrand = ThemeBrand.DEFAULT,
        darkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
        useDynamicColor = false,
        shouldHideOnboarding = false,
        contrast = Contrast.Normal
    )

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)

        repo = mockk(relaxed = true)
        userDataFlow = MutableStateFlow(initial)

        every { repo.userData } returns userDataFlow

        coEvery { repo.setThemeBrand(any()) } coAnswers {
            val brand = it.invocation.args[0] as ThemeBrand
            userDataFlow.value = userDataFlow.value.copy(themeBrand = brand)
        }
        coEvery { repo.setDarkThemeConfig(any()) } coAnswers {
            val cfg = it.invocation.args[0] as DarkThemeConfig
            userDataFlow.value = userDataFlow.value.copy(darkThemeConfig = cfg)
        }

        vm = SettingViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_setsSuccess_withInitialValues() = runTest {

        advanceUntilIdle()

        val s = vm.settingState.value
        assertTrue(s is SettingState.Success)
        s as SettingState.Success
        assertEquals(ThemeBrand.DEFAULT, s.themeBrand)
        assertEquals(DarkThemeConfig.FOLLOW_SYSTEM, s.darkThemeConfig)
    }

    @Test
    fun setThemeBrand_updates_repo_and_state() = runTest {
        advanceUntilIdle()

        vm.setThemeBrand(ThemeBrand.PINK)

        advanceUntilIdle()

        val s = vm.settingState.value
        assertTrue(s is SettingState.Success)
        s as SettingState.Success
        assertEquals(ThemeBrand.PINK, s.themeBrand)

        coVerify(exactly = 1) { repo.setThemeBrand(ThemeBrand.PINK) }
    }

    @Test
    fun setDarkThemeConfig_updates_repo_and_state() = runTest {
        advanceUntilIdle()

        vm.setDarkThemeConfig(DarkThemeConfig.DARK)

        advanceUntilIdle()

        val s = vm.settingState.value
        assertTrue(s is SettingState.Success)
        s as SettingState.Success
        assertEquals(DarkThemeConfig.DARK, s.darkThemeConfig)

        coVerify(exactly = 1) { repo.setDarkThemeConfig(DarkThemeConfig.DARK) }
    }
}