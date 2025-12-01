package com.example.quantumaccess.feature.auth.presentation

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.quantumaccess.core.designsystem.components.FingerprintPulseButton
import com.example.quantumaccess.core.designsystem.components.GoogleSignInButton
import com.example.quantumaccess.core.designsystem.components.InputField
import com.example.quantumaccess.core.designsystem.components.PrimaryActionButton
import com.example.quantumaccess.core.util.findFragmentActivity
import com.example.quantumaccess.data.local.SecurePrefsManager
import com.example.quantumaccess.core.designsystem.components.QuantumLogo
import com.example.quantumaccess.core.designsystem.theme.BorderSubtle
import com.example.quantumaccess.core.designsystem.theme.DeepBlue
import com.example.quantumaccess.core.designsystem.theme.NightBlack

@Composable
fun BiometricLoginScreen(
	modifier: Modifier = Modifier,
	onAuthenticate: () -> Unit = {},
    onGoogleSignIn: () -> Unit = {}, // Add if needed for actual implementation later
    onLoginWithPassword: (String, String) -> Unit = { _, _ -> onAuthenticate() } // Simplified for now
) {
	val context = LocalContext.current
	val executor = remember { ContextCompat.getMainExecutor(context) }
	val prefs = remember { SecurePrefsManager(context) }
	val biometricManager = remember { BiometricManager.from(context) }
	val activity = remember { context.findFragmentActivity() }

    // State for manual login fallback
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPasswordLogin by remember { mutableStateOf(false) }

	fun launchBiometric() {
		val allowed = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
		val canAuth = biometricManager.canAuthenticate(allowed)
		
        // Removed early return check for 'canAuth != SUCCESS' to allow system prompt to handle edge cases/setup if possible, 
        // or just to let 'authenticate' fail with error callback if something is wrong, providing better feedback.
		if (activity == null) {
			return
		}
		val prompt = BiometricPrompt(
			activity,
			executor,
			object : BiometricPrompt.AuthenticationCallback() {
				override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
					onAuthenticate()
				}
				override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    // Handle error
				}
				override fun onAuthenticationFailed() {
					// do nothing; user can retry
				}
			}
		)
		val promptInfo = BiometricPrompt.PromptInfo.Builder()
			.setTitle("Biometric authentication")
			.setSubtitle("Authenticate to continue")
			.setAllowedAuthenticators(allowed)
			.build()
		prompt.authenticate(promptInfo)
	}

	Box(
		modifier = modifier
			.fillMaxSize()
            .verticalScroll(rememberScrollState())
			.padding(horizontal = 24.dp, vertical = 24.dp)
	) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            QuantumLogo(
                iconSize = 40.dp,
                showText = true,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = DeepBlue,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(40.dp))

            // Biometric Button (Primary if enabled)
            if (prefs.isBiometricEnabled()) {
                FingerprintPulseButton(onClick = { launchBiometric() })
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tap to login with fingerprint",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // If fingerprint enabled, show OR divider, else just the buttons
            if (prefs.isBiometricEnabled()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Divider(modifier = Modifier.weight(1f), color = BorderSubtle)
                    Text(
                        text = "OR",
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Divider(modifier = Modifier.weight(1f), color = BorderSubtle)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Google Sign In
            GoogleSignInButton(
                onClick = { 
                    // Simulate Google Login -> Success
                    onAuthenticate() 
                },
                modifier = Modifier.fillMaxWidth(),
                text = "Sign in with Google"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Email/Password Login
            if (!showPasswordLogin) {
                Text(
                    text = "Log in with Email & Password",
                    color = DeepBlue,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { showPasswordLogin = true }
                        .padding(8.dp)
                )
            } else {
                 Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 6.dp,
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        InputField(
                            value = email,
                            onValueChange = { email = it },
                            label = "Email",
                            placeholder = "Enter your email",
                            labelIcon = Icons.Filled.Email
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        InputField(
                            value = password,
                            onValueChange = { password = it },
                            label = "Password",
                            placeholder = "Enter your password",
                            labelIcon = Icons.Filled.Lock,
                            isPassword = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PrimaryActionButton(
                            text = "Log In",
                            onClick = { onLoginWithPassword(email, password) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
	}
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BiometricLoginPreview() {
	BiometricLoginScreen(onAuthenticate = {})
}
