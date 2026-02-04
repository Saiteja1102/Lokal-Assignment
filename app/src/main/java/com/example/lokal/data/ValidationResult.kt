package com.example.lokal.data

sealed class ValidationResult {
    object Success : ValidationResult()
    object NoOtpGenerated : ValidationResult()
    object Expired : ValidationResult()
    object MaxAttemptsExceeded : ValidationResult()
    data class Incorrect(val attemptsRemaining: Int) : ValidationResult()
}