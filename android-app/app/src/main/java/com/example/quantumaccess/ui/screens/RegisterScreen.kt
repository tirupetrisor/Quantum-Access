package com.example.quantumaccess.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quantumaccess.ui.components.InputField
import com.example.quantumaccess.ui.components.QuantumButton
import com.example.quantumaccess.ui.components.QuantumLogo
import com.example.quantumaccess.ui.theme.DeepBlue

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
                color = Color(0xFF111111),
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
                            color = Color(0xFFE0E0E0),
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
                    InputField(
                        value = username,
                        onValueChange = { username = it },
                        label = "Username",
                        placeholder = "Choose a username",
                        labelIcon = Icons.Filled.AlternateEmail
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    InputField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Password",
                        placeholder = "Create a secure password",
                        labelIcon = Icons.Filled.Lock,
                        isPassword = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    InputField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirm Password",
                        placeholder = "Confirm your password",
                        labelIcon = Icons.Filled.Lock,
                        isPassword = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
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
                            tint = Color(0xFF2E7D32),
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
            QuantumButton(
                text = "Register & Continue",
                loading = loading,
                onClick = {
                    if (!loading) {
                        loading = true
                        onRegister(fullName, username, password, biometricEnabled)
                        loading = false
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

