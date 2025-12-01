package com.example.quantumaccess.feature.auth.presentation

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.example.quantumaccess.core.designsystem.components.FingerprintPulseButton
import com.example.quantumaccess.core.designsystem.components.PrimaryActionButton
import com.example.quantumaccess.core.designsystem.components.QuantumLogo
import com.example.quantumaccess.core.designsystem.theme.DeepBlue
import com.example.quantumaccess.core.designsystem.theme.NightBlack
import com.example.quantumaccess.core.util.findActivity
import com.example.quantumaccess.core.util.findFragmentActivity

@Composable
fun BiometricSetupScreen(
    modifier: Modifier = Modifier,
    onEnable: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    val context = LocalContext.current
    val executor = remember { ContextCompat.getMainExecutor(context) }
    val biometricManager = remember { BiometricManager.from(context) }
    val activity = remember { context.findFragmentActivity() }

    // Force status bar icons to be dark (visible on white background) for this screen
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = context.findActivity()?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                // Set to true (dark icons) for this screen
                controller.isAppearanceLightStatusBars = true
            }
        }
    }
    
    // Function to actually trigger system enrollment prompt or just verify and enable
    fun enableBiometric() {
        val allowed = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        val canAuth = biometricManager.canAuthenticate(allowed)
        
        if (activity != null) {
             // Verify user identity before enabling
             val prompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        onEnable()
                    }
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                         // Handle error or cancel
                    }
                }
            )
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Verify Identity")
                .setSubtitle("Confirm your fingerprint to enable biometric login")
                .setAllowedAuthenticators(allowed)
                .build()
            prompt.authenticate(promptInfo)
        } else {
            // If not available, maybe just skip or show error
            // For now, just call onEnable (simulating success or user intent) if checking fails in demo
            onEnable() 
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp)
        ) {
            QuantumLogo(iconSize = 40.dp, showText = true)
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Enable Fingerprint Login",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = NightBlack,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Use your fingerprint for faster and more secure access to your account. You can always change this later in settings.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            FingerprintPulseButton(onClick = { enableBiometric() })
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tap to verify and enable fingerprint login",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryActionButton(
                text = "Enable Fingerprint",
                onClick = { enableBiometric() },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Skip for now",
                    color = DeepBlue,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BiometricSetupPreview() {
    BiometricSetupScreen()
}

