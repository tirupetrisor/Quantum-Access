package com.example.quantumaccess.feature.auth.presentation

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.quantumaccess.core.designsystem.components.GoogleSignInButton
import com.example.quantumaccess.core.designsystem.components.InputField
import com.example.quantumaccess.core.designsystem.components.PrimaryActionButton
import com.example.quantumaccess.core.designsystem.components.QuantumLogo
import com.example.quantumaccess.core.designsystem.theme.AlertRed
import com.example.quantumaccess.core.designsystem.theme.BorderSubtle
import com.example.quantumaccess.core.designsystem.theme.DeepBlue
import com.example.quantumaccess.core.designsystem.theme.ForestGreen
import com.example.quantumaccess.core.designsystem.theme.NightBlack
import com.example.quantumaccess.feature.auth.data.GoogleAuthClient
import com.example.quantumaccess.viewmodel.RegisterUiState
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    uiState: RegisterUiState = RegisterUiState(),
    onRegister: (fullName: String, username: String, email: String, password: String, enableBiometric: Boolean) -> Unit = { _, _, _, _, _ -> },
    onGoogleSignInSuccess: (email: String, name: String, googleId: String, biometricEnabled: Boolean) -> Unit = { _, _, _, _ -> },
    onLoginLink: () -> Unit = {},
    onClearErrors: () -> Unit = {}
) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    // Local validation state
    var nameError by remember { mutableStateOf<String?>(null) }
    var usernameLocalError by remember { mutableStateOf<String?>(null) }
    var emailLocalError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    
    // Real Google Auth Client
    val context = LocalContext.current
    val googleAuthClient = remember {
        GoogleAuthClient(
            context = context,
            oneTapClient = Identity.getSignInClient(context)
        )
    }
    
    val coroutineScope = rememberCoroutineScope()
    var isGoogleLoading by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                coroutineScope.launch {
                    val signInResult = googleAuthClient.signInWithIntent(result.data ?: return@launch)
                    val data = signInResult.data
                    if (data != null) {
                        onGoogleSignInSuccess(data.email, data.username, data.userId, false)
                        isGoogleLoading = false
                    } else {
                         // Handle error or show toast (ideally pass error callback)
                         isGoogleLoading = false
                    }
                }
            } else {
                isGoogleLoading = false
            }
        }
    )

    // Clear VM errors when user types
    LaunchedEffect(fullName, username, email, password) {
        if (uiState.usernameError != null || uiState.emailError != null || uiState.generalError != null) {
            onClearErrors()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                QuantumLogo(iconSize = 40.dp, showText = true)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Create Your Account",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = NightBlack,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            GoogleSignInButton(
                onClick = { 
                    isGoogleLoading = true
                    coroutineScope.launch {
                        val signInIntentSender = googleAuthClient.signIn()
                        if (signInIntentSender != null) {
                            launcher.launch(
                                IntentSenderRequest.Builder(signInIntentSender).build()
                            )
                        } else {
                            isGoogleLoading = false
                        }
                    }
                },
                loading = isGoogleLoading || (uiState.isLoading && !uiState.emailError.isNullOrEmpty()),
                enabled = !uiState.isLoading && !isGoogleLoading,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (uiState.generalError != null) {
                Text(
                    text = uiState.generalError,
                    color = AlertRed,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 6.dp,
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = BorderSubtle,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp)
                ) {
                    InputField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = "Full Name",
                        placeholder = "Enter your full name",
                        labelIcon = Icons.Filled.Person
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (nameError != null) {
                        ErrorMessage(nameError!!)
                    }

                    InputField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email",
                        placeholder = "Enter your email",
                        labelIcon = Icons.Filled.Email
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Show either local or server-side email error
                    val displayEmailError = emailLocalError ?: uiState.emailError
                    if (displayEmailError != null) {
                        ErrorMessage(displayEmailError)
                    }

                    InputField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Username",
                        placeholder = "Choose a username",
                        labelIcon = Icons.Filled.AlternateEmail
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    // Show either local or server-side username error
                    val displayUsernameError = usernameLocalError ?: uiState.usernameError
                    if (displayUsernameError != null) {
                        ErrorMessage(displayUsernameError)
                    }

                    InputField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        placeholder = "Create a secure password",
                        labelIcon = Icons.Filled.Lock,
                        isPassword = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (passwordError != null) {
                        ErrorMessage(passwordError!!)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            PrimaryActionButton(
                text = "Register & Continue",
                loading = uiState.isLoading,
                onClick = {
                    if (!uiState.isLoading) {
                        nameError = if (fullName.isBlank()) "Name is required" else null
                        emailLocalError = if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) "Valid email is required" else null
                        usernameLocalError = if (username.length < 3) "Username must be at least 3 characters" else null
                        passwordError = if (password.length < 6) "Password must be at least 6 characters" else null

                        val valid = listOf(nameError, emailLocalError, usernameLocalError, passwordError).all { it == null }
                        if (valid) {
                            // Pass false for biometricEnabled to delegate setup to the next screen
                            onRegister(fullName, username, email, password, false)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Already have an account? Log in",
                color = DeepBlue,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxWidth()
                    .clickable { onLoginLink() },
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorMessage(text: String) {
    Text(
        text = text,
        color = AlertRed,
        style = TextStyle(fontSize = 12.sp)
    )
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun secureGreenCheckColor(): Color = Color(0xFF4CAF50)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RegisterPreview() {
    RegisterScreen()
}
