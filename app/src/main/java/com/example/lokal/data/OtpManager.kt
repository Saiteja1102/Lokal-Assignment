package com.example.lokal.data

import kotlin.random.Random

class OtpManager {
    // Store OTP data per email
    private val otpStore = mutableMapOf<String, OtpData>()

    companion object {
        private const val OTP_LENGTH = 6
        private const val OTP_EXPIRY_SECONDS = 60L
        private const val MAX_ATTEMPTS = 3
    }

    /**
     * Generates a new 6-digit OTP for the given email
     * Invalidates any existing OTP and resets attempt count
     */
    fun generateOtp(email: String): String {
        val otp = Random.nextInt(100000, 999999).toString()
        val expiryTime = System.currentTimeMillis() + (OTP_EXPIRY_SECONDS * 1000)

        otpStore[email] = OtpData(
            otp = otp,
            expiryTimeMillis = expiryTime,
            attemptsRemaining = MAX_ATTEMPTS
        )

        return otp
    }

    /**
     * Validates OTP for given email
     * Returns ValidationResult with success/failure reason
     */
    fun validateOtp(email: String, enteredOtp: String): ValidationResult {
        val otpData = otpStore[email] ?: return ValidationResult.NoOtpGenerated

        // Check expiry first
        if (otpData.isExpired()) {
            return ValidationResult.Expired
        }

        // Check attempts
        if (otpData.attemptsRemaining <= 0) {
            return ValidationResult.MaxAttemptsExceeded
        }

        // Validate OTP
        return if (otpData.otp == enteredOtp) {
            // Clear OTP on success
            otpStore.remove(email)
            ValidationResult.Success
        } else {
            // Decrement attempts
            otpStore[email] = otpData.copy(
                attemptsRemaining = otpData.attemptsRemaining - 1
            )
            ValidationResult.Incorrect(otpData.attemptsRemaining - 1)
        }
    }

    /**
     * Gets current OTP data for an email (for displaying countdown, etc.)
     */
    fun getOtpData(email: String): OtpData? {
        return otpStore[email]?.takeIf { !it.isExpired() }
    }

    /**
     * Clears OTP for an email
     */
    fun clearOtp(email: String) {
        otpStore.remove(email)
    }
}