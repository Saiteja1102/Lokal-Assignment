// data/OtpData.kt
package com.example.lokal.data

data class OtpData(
    val otp: String,
    val expiryTimeMillis: Long,
    val attemptsRemaining: Int = 3,
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    fun isExpired(): Boolean {
        return System.currentTimeMillis() > expiryTimeMillis
    }

    fun remainingTimeSeconds(): Long {
        val remaining = (expiryTimeMillis - System.currentTimeMillis()) / 1000
        return if (remaining > 0) remaining else 0
    }
}