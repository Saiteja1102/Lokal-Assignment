package com.example.lokal.analytics

import timber.log.Timber

object AnalyticsLogger {

    fun initialize() {
        Timber.plant(Timber.DebugTree())
    }

    fun logOtpGenerated(email: String) {
        Timber.d("EVENT: OTP_GENERATED for email: $email")
    }

    fun logOtpValidationSuccess(email: String) {
        Timber.d("EVENT: OTP_VALIDATION_SUCCESS for email: $email")
    }

    fun logOtpValidationFailure(email: String, reason: String) {
        Timber.d("EVENT: OTP_VALIDATION_FAILURE for email: $email, reason: $reason")
    }

    fun logLogout(email: String, sessionDurationSeconds: Long) {
        Timber.d("EVENT: LOGOUT for email: $email, duration: ${sessionDurationSeconds}s")
    }
}