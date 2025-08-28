package com.parinexus.main

import com.parinexus.testing.repository.TestIAlarmManager
import com.parinexus.testing.repository.TestUserDataRepository
import com.parinexus.testing.util.MainDispatcherRule
import com.parinexus.testing.util.TestAnalyticsLogger
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val analyticsHelper = TestAnalyticsLogger()
    private val userDataRepository = TestUserDataRepository()
    private val noteRepository = TestNotePadRepository()
    private val alarmManager = TestIAlarmManager()
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
//        viewModel = MainViewModel(
//
//        )
    }

    @Test
    fun stateIsInitiallyLoading() = runTest(mainDispatcherRule.testDispatcher) {
//        viewModel
//            .feedUiMainState
//            .test {
//                var state = awaitItem()
//
//                assertTrue(state is Result.Loading)
//
//                state = awaitItem()
//
//                assertTrue(state is Result.Success)
//
//                assertEquals(
//                    10,
//                    state.data.size,
//
//                )
//
//                cancelAndIgnoreRemainingEvents()
//            }
    }
}
