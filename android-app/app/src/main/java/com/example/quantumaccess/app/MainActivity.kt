package com.example.quantumaccess.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.quantumaccess.app.navigation.AppNavGraph
import com.example.quantumaccess.core.designsystem.theme.QuantumAccessTheme

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
    AppNavGraph()
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MainPreview() {
    QuantumAccessTheme {
        MainScreen()
    }
}