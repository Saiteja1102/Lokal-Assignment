package com.example.lokal.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.lokal.viewmodel.OtpUiState

@Composable
fun OtpScreen(
    email: String,
    generatedOtp: String,
    otpUiState: OtpUiState,
    onValidateOtp: (String) -> Unit,
    onResendOtp: () -> Unit,
    errorMessage: String?,
    onClearError: () -> Unit
) {
    var otpInput by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }

    // Clear error when user starts typing
    LaunchedEffect(otpInput) {
        if (errorMessage != null) {
            onClearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())  // ✅ Added scrolling
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Enter OTP",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "OTP sent to $email",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Display generated OTP (for demo purposes)
        Card(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your OTP",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = generatedOtp,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Countdown timer
        if (!otpUiState.isExpired) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Time remaining: ${formatTime(otpUiState.remainingTimeSeconds)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (otpUiState.remainingTimeSeconds <= 10) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                Text(
                    text = "Attempts: ${otpUiState.attemptsRemaining}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Visual countdown progress
            LinearProgressIndicator(
                progress = { otpUiState.remainingTimeSeconds / 60f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            )
        } else {
            Text(
                text = "OTP Expired",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = otpInput,
            onValueChange = {
                if (it.text.length <= 6 && it.text.all { char -> char.isDigit() }) {
                    otpInput = it
                }
            },
            label = { Text("Enter 6-digit OTP") },
            singleLine = true,
            isError = errorMessage != null,
            enabled = !otpUiState.isExpired && otpUiState.attemptsRemaining > 0,
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .align(Alignment.Start)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onValidateOtp(otpInput.text) },
            modifier = Modifier.fillMaxWidth(),
            enabled = otpInput.text.length == 6 && !otpUiState.isExpired && otpUiState.attemptsRemaining > 0
        ) {
            Text("Verify OTP")
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onResendOtp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Resend OTP")
        }
    }
}

private fun formatTime(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}