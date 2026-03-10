package com.example.moodmate.viewmodel

import android.content.Context
import com.example.moodmate.local.TokenManager
import com.example.moodmate.notification.NotificationScheduler
import com.example.moodmate.util.LocaleHelper
import com.example.moodmate.notification.NotificationPreferenceHelper
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var tokenManager: TokenManager
    private lateinit var notificationScheduler: NotificationScheduler
    private lateinit var context: Context

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        tokenManager = mockk(relaxed = true)
        notificationScheduler = mockk(relaxed = true)
        context = mockk(relaxed = true)

        mockkObject(LocaleHelper)
        mockkObject(NotificationPreferenceHelper)

        every { LocaleHelper.getLanguage(any()) } returns "tr"
        every { NotificationPreferenceHelper.isNotificationEnabled(any()) } returns true

        viewModel = ProfileViewModel(tokenManager, notificationScheduler, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun init_shouldLoadCurrentSettings() {
        assertEquals("Türkçe", viewModel.uiState.value.selectedLanguage)
        assertTrue(viewModel.uiState.value.notificationEnabled)
    }

    @Test
    fun setNotification_whenEnabled_shouldScheduleNotifications() {
        viewModel.setNotification(true)

        assertTrue(viewModel.uiState.value.notificationEnabled)
        verify { NotificationPreferenceHelper.setNotificationEnabled(context, true) }
        verify { notificationScheduler.scheduleDailyNotifications() }
    }

    @Test
    fun setNotification_whenDisabled_shouldCancelNotifications() {
        viewModel.setNotification(false)

        assertFalse(viewModel.uiState.value.notificationEnabled)
        verify { NotificationPreferenceHelper.setNotificationEnabled(context, false) }
        verify { notificationScheduler.cancelAllNotifications() }
    }

    @Test
    fun setLanguage_whenDifferentLanguage_shouldUpdateAndTriggerRecreate() {
        viewModel.setLanguage("English")

        assertEquals("English", viewModel.uiState.value.selectedLanguage)
        assertTrue(viewModel.shouldRecreateActivity.value)
        verify { LocaleHelper.saveLanguage(context, "en") }
    }

    @Test
    fun setLanguage_whenSameLanguage_shouldNotTriggerRecreate() {
        viewModel.setLanguage("Türkçe")

        assertEquals("Türkçe", viewModel.uiState.value.selectedLanguage)
        assertFalse(viewModel.shouldRecreateActivity.value)
    }

    @Test
    fun setLanguage_shouldMapLanguageNamesToCodes() {
        viewModel.setLanguage("English")
        verify { LocaleHelper.saveLanguage(context, "en") }

        viewModel.setLanguage("Español")
        verify { LocaleHelper.saveLanguage(context, "es") }

        viewModel.setLanguage("Italiano")
        verify { LocaleHelper.saveLanguage(context, "it") }
    }

    @Test
    fun logout_shouldClearTokenAndNavigateToLogin() = runTest {
        viewModel.logout()

        assertTrue(viewModel.uiState.value.shouldNavigateToLogin)
        coVerify { tokenManager.clearUser() }
    }

    @Test
    fun resetRecreateFlag_shouldResetFlag() {
        viewModel.setLanguage("English")

        viewModel.resetRecreateFlag()

        assertFalse(viewModel.shouldRecreateActivity.value)
    }

    @Test
    fun resetNavigationFlag_shouldResetFlag() = runTest {
        viewModel.logout()

        viewModel.resetNavigationFlag()

        assertFalse(viewModel.uiState.value.shouldNavigateToLogin)
    }
}