package com.example.moodmate.viewmodel

import android.content.Context
import com.example.moodmate.R
import com.example.moodmate.data.AuthResponse
import com.example.moodmate.local.TokenManager
import com.example.moodmate.repository.AdviceRepository
import com.example.moodmate.repository.AuthRepository
import com.example.moodmate.repository.MoodRepository
import com.example.moodmate.util.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignInViewModelTest {

    private lateinit var viewModel: SignInViewModel
    private lateinit var authRepository: AuthRepository
    private lateinit var tokenManager: TokenManager
    private lateinit var moodRepository: MoodRepository
    private lateinit var adviceRepository: AdviceRepository
    private lateinit var context: Context

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        authRepository = mockk()
        tokenManager = mockk(relaxed = true)
        moodRepository = mockk(relaxed = true)
        adviceRepository = mockk(relaxed = true)
        context = mockk(relaxed = true)

        every { context.getString(R.string.error_sign_in_failed) } returns "Login failed"
        coEvery { tokenManager.userId } returns flowOf(null)

        viewModel = SignInViewModel(authRepository, tokenManager, moodRepository, adviceRepository, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onEmailChange_shouldUpdateEmailAndClearError() {
        viewModel.onEmailChange("test@example.com")

        assertEquals("test@example.com", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.validationErrors.emailError)
    }

    @Test
    fun onPasswordChange_shouldUpdatePasswordAndClearError() {
        viewModel.onPasswordChange("password123")

        assertEquals("password123", viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.validationErrors.passwordError)
    }

    @Test
    fun togglePasswordVisibility_shouldToggleVisibility() {
        val initialVisibility = viewModel.uiState.value.isPasswordVisible

        viewModel.togglePasswordVisibility()

        assertEquals(!initialVisibility, viewModel.uiState.value.isPasswordVisible)
    }

    @Test
    fun login_whenSuccessful_shouldSaveTokenAndSetSuccess() = runTest {
        val authResponse = AuthResponse(
            id = 1L,
            firstName = "Yılmaz",
            lastName = "Naycı",
            email = "test@example.com",
            token = "test-token",
            createdAt = "2024-01-01T00:00:00",
            updatedAt = "2024-01-01T00:00:00"
        )
        coEvery { authRepository.login(any(), any()) } returns Resource.Success(authResponse)

        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")
        viewModel.login()

        assertTrue(viewModel.actionState.value.isSuccess)
        assertFalse(viewModel.actionState.value.isLoading)
        coVerify {
            tokenManager.saveUser(
                token = "test-token",
                userId = 1L,
                email = "test@example.com",
                firstName = "Yılmaz",
                lastName = "Naycı"
            )
        }
    }

    @Test
    fun login_whenDifferentUserLoggedIn_shouldClearPreviousUserData() = runTest {
        coEvery { tokenManager.userId } returns flowOf(99L)
        val authResponse = AuthResponse(
            id = 1L,
            firstName = "Yılmaz",
            lastName = "Naycı",
            email = "test@example.com",
            token = "test-token",
            createdAt = "2024-01-01T00:00:00",
            updatedAt = "2024-01-01T00:00:00"
        )
        coEvery { authRepository.login(any(), any()) } returns Resource.Success(authResponse)

        viewModel = SignInViewModel(authRepository, tokenManager, moodRepository, adviceRepository, context)
        viewModel.login()

        coVerify { moodRepository.clearAllMoodsForUser() }
        coVerify { adviceRepository.clearAdviceForUser() }
    }

    @Test
    fun login_whenFailed_shouldShowError() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Resource.Error("Invalid credentials")

        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("wrongpassword")
        viewModel.login()

        assertFalse(viewModel.actionState.value.isSuccess)
        assertFalse(viewModel.actionState.value.isLoading)
        assertNotNull(viewModel.uiState.value.validationErrors.emailError)
    }

    @Test
    fun login_whenValidationError_shouldShowFieldErrors() = runTest {
        val fieldErrors = mapOf(
            "email" to "Email is required",
            "password" to "Password is required"
        )
        coEvery { authRepository.login(any(), any()) } returns Resource.Error(
            message = "Validation failed",
            fieldErrors = fieldErrors
        )

        viewModel.login()

        assertEquals("Email is required", viewModel.uiState.value.validationErrors.emailError)
        assertEquals("Password is required", viewModel.uiState.value.validationErrors.passwordError)
    }
}