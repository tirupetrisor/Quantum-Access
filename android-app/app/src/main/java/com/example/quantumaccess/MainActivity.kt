package com.example.quantumaccess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.quantumaccess.nav.AppNavGraph
import com.example.quantumaccess.ui.theme.QuantumAccessTheme

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