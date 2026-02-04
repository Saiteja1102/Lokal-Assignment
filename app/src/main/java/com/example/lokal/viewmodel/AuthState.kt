package com.example.lokal.viewmodel

sealed class AuthState {
    object Initial : AuthState()
    data class OtpSent(
        val email: String,
        val generatedOtp: String
    ) : AuthState()
    data class Authenticated(
        val email: String,
        val sessionStartTime: Long
    ) : AuthState()
}