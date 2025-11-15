package com.example.quantumaccess.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.quantumaccess.ui.screens.BiometricLoginScreen
import com.example.quantumaccess.ui.screens.LocationVerificationScreen
import com.example.quantumaccess.ui.screens.RegisterScreen
import com.example.quantumaccess.ui.screens.SplashScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.Splash) {
        composable(Routes.Splash) {
            SplashScreen(
                onContinue = {
                    navController.navigate(Routes.Register) {
                        popUpTo(Routes.Splash) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.Register) {
            RegisterScreen(
                onLoginLink = {
                    navController.navigate(Routes.BiometricLogin)
                }
            )
        }
		composable(Routes.BiometricLogin) {
			BiometricLoginScreen(onAuthenticate = { })
		}
		composable(Routes.LocationVerification) {
			LocationVerificationScreen()
		}
    }
}

