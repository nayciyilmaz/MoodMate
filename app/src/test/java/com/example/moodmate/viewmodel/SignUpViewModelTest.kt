package com.example.moodmate.viewmodel

import android.content.Context
import com.example.moodmate.R
import com.example.moodmate.data.AuthResponse
import com.example.moodmate.repository.AuthRepository
import com.example.moodmate.util.Resource
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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
class SignUpViewModelTest {

    private lateinit var viewModel: SignUpViewModel
    private lateinit var authRepository: AuthRepository
    private lateinit var context: Context

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        authRepository = mockk()
        context = mockk(relaxed = true)

        every { context.getString(R.string.error_sign_up_failed) } returns "Registration failed"

        viewModel = SignUpViewModel(authRepository, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun onFirstNameChange_shouldUpdateFirstNameAndClearError() {
        viewModel.onFirstNameChange("Yılmaz")

        assertEquals("Yılmaz", viewModel.uiState.value.firstName)
        assertNull(viewModel.uiState.value.validationErrors.firstNameError)
    }

    @Test
    fun onLastNameChange_shouldUpdateLastNameAndClearError() {
        viewModel.onLastNameChange("Naycı")

        assertEquals("Naycı", viewModel.uiState.value.lastName)
        assertNull(viewModel.uiState.value.validationErrors.lastNameError)
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
    fun register_whenSuccessful_shouldSetSuccessState() = runTest {
        val authResponse = AuthResponse(
            id = 1L,
            firstName = "Yılmaz",
            lastName = "Naycı",
            email = "test@example.com",
            token = "test-token",
            createdAt = "2024-01-01T00:00:00",
            updatedAt = "2024-01-01T00:00:00"
        )
        coEvery { authRepository.register(any(), any(), any(), any()) } returns Resource.Success(authResponse)

        viewModel.onFirstNameChange("Yılmaz")
        viewModel.onLastNameChange("Naycı")
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")
        viewModel.register()

        assertTrue(viewModel.actionState.value.isSuccess)
        assertFalse(viewModel.actionState.value.isLoading)
    }

    @Test
    fun register_whenValidationError_shouldShowFieldErrors() = runTest {
        val fieldErrors = mapOf(
            "first_name" to "First name is required",
            "last_name" to "Last name is required",
            "email" to "Invalid email",
            "password" to "Password too short"
        )
        coEvery { authRepository.register(any(), any(), any(), any()) } returns Resource.Error(
            message = "Validation failed",
            fieldErrors = fieldErrors
        )

        viewModel.register()

        assertEquals("First name is required", viewModel.uiState.value.validationErrors.firstNameError)
        assertEquals("Last name is required", viewModel.uiState.value.validationErrors.lastNameError)
        assertEquals("Invalid email", viewModel.uiState.value.validationErrors.emailError)
        assertEquals("Password too short", viewModel.uiState.value.validationErrors.passwordError)
    }

    @Test
    fun register_whenFailed_shouldShowError() = runTest {
        coEvery { authRepository.register(any(), any(), any(), any()) } returns Resource.Error("Email already exists")

        viewModel.register()

        assertFalse(viewModel.actionState.value.isSuccess)
        assertNotNull(viewModel.uiState.value.validationErrors.emailError)
    }
}