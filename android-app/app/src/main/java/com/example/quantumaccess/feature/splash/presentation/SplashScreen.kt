package com.example.quantumaccess.feature.splash.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quantumaccess.core.designsystem.components.QuantumLogoNeonCard
import com.example.quantumaccess.core.designsystem.theme.DeepBlue
import com.example.quantumaccess.core.designsystem.theme.SecureGreen
import com.example.quantumaccess.core.designsystem.theme.Silver
import kotlinx.coroutines.delay

private val BackgroundBlue = DeepBlue
private val NeonGreen = SecureGreen

@Composable
fun SplashScreen(modifier: Modifier = Modifier, onContinue: () -> Unit = {}) {
    var showTagline by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500)
        showTagline = true
        delay(500)
        onContinue()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlue),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            QuantumLogoNeonCard()
            Spacer(modifier = Modifier.height(24.dp))
            BrandTitle()
            Spacer(modifier = Modifier.height(12.dp))
            AnimatedVisibility(visible = showTagline, enter = fadeIn(), exit = fadeOut()) {
                Text(
                    text = "Unlock the Unknown",
                    color = Silver.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun BrandTitle() {
    val gradient = Brush.linearGradient(listOf(Silver, NeonGreen))
    val text = buildAnnotatedString {
        withStyle(SpanStyle(brush = gradient, fontWeight = FontWeight.Bold)) {
            append("Quantum")
        }
        withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
            append("Access")
        }
    }
    Text(
        text = text,
        fontSize = 36.sp,
        letterSpacing = 0.5.sp
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SplashPreview() {
    SplashScreen()
}


