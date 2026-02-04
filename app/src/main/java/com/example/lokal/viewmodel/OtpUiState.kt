package com.example.lokal.viewmodel

data class OtpUiState(
    val remainingTimeSeconds: Long = 0,
    val attemptsRemaining: Int = 3,
    val isExpired: Boolean = false
)