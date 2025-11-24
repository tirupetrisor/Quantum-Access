package com.example.quantumaccess.feature.auth.presentation

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quantumaccess.core.designsystem.components.InputField
import com.example.quantumaccess.core.designsystem.components.PrimaryActionButton
import com.example.quantumaccess.core.designsystem.components.QuantumLogo
import com.example.quantumaccess.core.designsystem.theme.AlertRed
import com.example.quantumaccess.core.designsystem.theme.BorderSubtle
import com.example.quantumaccess.core.designsystem.theme.DeepBlue
import com.example.quantumaccess.core.designsystem.theme.ForestGreen
import com.example.quantumaccess.core.designsystem.theme.NightBlack
import com.example.quantumaccess.core.designsystem.theme.Slate800
import com.example.quantumaccess.data.local.PreferencesManager

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onRegister: (fullName: String, username: String, password: String, enableBiometric: Boolean) -> Unit = { _, _, _, _ -> },
    onLoginLink: () -> Unit = {}
) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var biometricEnabled by remember { mutableStateOf(true) }
    var loading by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val prefs = remember { PreferencesManager(context) }

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
                        Text(
                            text = nameError ?: "",
                            color = AlertRed,
                            style = TextStyle(fontSize = 12.sp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    InputField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Username",
                        placeholder = "Choose a username",
                        labelIcon = Icons.Filled.AlternateEmail
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (usernameError != null) {
                        Text(
                            text = usernameError ?: "",
                            color = AlertRed,
                            style = TextStyle(fontSize = 12.sp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
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
                        Text(
                            text = passwordError ?: "",
                            color = AlertRed,
                            style = TextStyle(fontSize = 12.sp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    InputField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirm Password",
                        placeholder = "Confirm your password",
                        labelIcon = Icons.Filled.Lock,
                        isPassword = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (confirmError != null) {
                        Text(
                            text = confirmError ?: "",
                            color = AlertRed,
                            style = TextStyle(fontSize = 12.sp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Checkbox(
                            checked = biometricEnabled,
                            onCheckedChange = { biometricEnabled = it },
                            colors = CheckboxDefaults.colors(checkedColor = secureGreenCheckColor())
                        )
                        Icon(
                            imageVector = Icons.Filled.Fingerprint,
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "  Enable biometric login (fingerprint)",
                            color = Color(0xFF424242),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            PrimaryActionButton(
                text = "Register & Continue",
                loading = loading,
                onClick = {
                    if (!loading) {
                        nameError = if (fullName.isBlank()) "Name is required" else null
                        usernameError = if (username.length < 3) "Username must be at least 3 characters" else null
                        passwordError = if (password.length < 6) "Password must be at least 6 characters" else null
                        confirmError = if (confirmPassword != password) "Passwords do not match" else null

                        val valid = listOf(nameError, usernameError, passwordError, confirmError).all { it == null }
                        if (valid) {
                            // verificare locală de unicitate pentru username
                            val normalized = username.trim().lowercase()
                            if (prefs.registeredUsernames.contains(normalized)) {
                                usernameError = "Username already exists (local)"
                                return@PrimaryActionButton
                            }
                            loading = true
                            onRegister(fullName, username, password, biometricEnabled)
                            // revenim imediat din loading pentru a evita blocarea la erori
                            loading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Already have an account? Log in with fingerprint",
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
private fun secureGreenCheckColor(): Color = Color(0xFF4CAF50)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun RegisterPreview() {
    RegisterScreen()
}

