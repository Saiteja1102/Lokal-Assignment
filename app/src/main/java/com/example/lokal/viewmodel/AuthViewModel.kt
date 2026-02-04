package com.example.lokal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lokal.analytics.AnalyticsLogger
import com.example.lokal.data.OtpManager
import com.example.lokal.data.ValidationResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val otpManager = OtpManager()

    // Main authentication state
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // OTP UI state (countdown, attempts)
    private val _otpUiState = MutableStateFlow(OtpUiState())
    val otpUiState: StateFlow<OtpUiState> = _otpUiState.asStateFlow()

    // Error messages
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var countdownJob: Job? = null

    /**
     * Validates email format
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Generates OTP for given email
     */
    fun generateOtp(email: String) {
        // Clear any previous error
        _errorMessage.value = null

        // Validate email
        if (email.isBlank()) {
            _errorMessage.value = "Please enter an email address"
            return
        }

        if (!isValidEmail(email)) {
            _errorMessage.value = "Please enter a valid email address"
            return
        }

        // Generate OTP
        val otp = otpManager.generateOtp(email)

        // Log event
        AnalyticsLogger.logOtpGenerated(email)

        // Update state
        _authState.value = AuthState.OtpSent(
            email = email,
            generatedOtp = otp
        )

        // Start countdown timer
        startOtpCountdown(email)
    }

    /**
     * Starts countdown timer for OTP expiry
     */
    private fun startOtpCountdown(email: String) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (true) {
                val otpData = otpManager.getOtpData(email)

                if (otpData == null) {
                    // OTP expired or cleared
                    _otpUiState.value = OtpUiState(
                        remainingTimeSeconds = 0,
                        attemptsRemaining = 0,
                        isExpired = true
                    )
                    break
                }

                val remainingSeconds = otpData.remainingTimeSeconds()

                _otpUiState.value = OtpUiState(
                    remainingTimeSeconds = remainingSeconds,
                    attemptsRemaining = otpData.attemptsRemaining,
                    isExpired = otpData.isExpired()
                )

                if (remainingSeconds <= 0) {
                    break
                }

                delay(1000) // Update every second
            }
        }
    }

    /**
     * Validates entered OTP
     */
    fun validateOtp(email: String, enteredOtp: String) {
        _errorMessage.value = null

        if (enteredOtp.isBlank()) {
            _errorMessage.value = "Please enter OTP"
            return
        }

        if (enteredOtp.length != 6) {
            _errorMessage.value = "OTP must be 6 digits"
            return
        }

        val result = otpManager.validateOtp(email, enteredOtp)

        when (result) {
            is ValidationResult.Success -> {
                AnalyticsLogger.logOtpValidationSuccess(email)
                countdownJob?.cancel()
                _authState.value = AuthState.Authenticated(
                    email = email,
                    sessionStartTime = System.currentTimeMillis()
                )
            }
            is ValidationResult.Incorrect -> {
                val attemptsLeft = result.attemptsRemaining
                AnalyticsLogger.logOtpValidationFailure(email, "Incorrect OTP, $attemptsLeft attempts remaining")
                _errorMessage.value = "Incorrect OTP. $attemptsLeft attempt(s) remaining"
            }
            is ValidationResult.Expired -> {
                AnalyticsLogger.logOtpValidationFailure(email, "OTP Expired")
                _errorMessage.value = "OTP has expired. Please request a new one"
            }
            is ValidationResult.MaxAttemptsExceeded -> {
                AnalyticsLogger.logOtpValidationFailure(email, "Max attempts exceeded")
                _errorMessage.value = "Maximum attempts exceeded. Please request a new OTP"
            }
            is ValidationResult.NoOtpGenerated -> {
                AnalyticsLogger.logOtpValidationFailure(email, "No OTP generated")
                _errorMessage.value = "No OTP found. Please request a new one"
            }
        }
    }

    /**
     * Logs out current user
     */
    fun logout() {
        val currentState = _authState.value
        if (currentState is AuthState.Authenticated) {
            val sessionDuration = (System.currentTimeMillis() - currentState.sessionStartTime) / 1000
            AnalyticsLogger.logLogout(currentState.email, sessionDuration)
        }

        countdownJob?.cancel()
        _authState.value = AuthState.Initial
        _otpUiState.value = OtpUiState()
        _errorMessage.value = null
    }

    /**
     * Clears error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}