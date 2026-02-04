package com.example.lokal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lokal.ui.LoginScreen
import com.example.lokal.ui.OtpScreen
import com.example.lokal.ui.SessionScreen
import com.example.lokal.ui.theme.LokalTheme
import com.example.lokal.viewmodel.AuthState
import com.example.lokal.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            LokalTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AuthApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AuthApp(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val otpUiState by viewModel.otpUiState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    when (val state = authState) {
        is AuthState.Initial -> {
            LoginScreen(
                onSendOtp = { email -> viewModel.generateOtp(email) },
                errorMessage = errorMessage,
                onClearError = { viewModel.clearError() }
            )
        }

        is AuthState.OtpSent -> {
            OtpScreen(
                email = state.email,
                generatedOtp = state.generatedOtp,
                otpUiState = otpUiState,
                onValidateOtp = { otp -> viewModel.validateOtp(state.email, otp) },
                onResendOtp = { viewModel.generateOtp(state.email) },
                errorMessage = errorMessage,
                onClearError = { viewModel.clearError() }
            )
        }

        is AuthState.Authenticated -> {
            SessionScreen(
                email = state.email,
                sessionStartTime = state.sessionStartTime,
                onLogout = { viewModel.logout() }
            )
        }
    }
}