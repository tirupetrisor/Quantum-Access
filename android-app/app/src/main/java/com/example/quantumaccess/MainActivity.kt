package com.example.quantumaccess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.quantumaccess.ui.theme.QuantumAccessTheme
import com.example.quantumaccess.ui.screens.SplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuantumAccessTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    SplashScreen()
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainPreview() {
    QuantumAccessTheme {
        MainScreen()
    }
}