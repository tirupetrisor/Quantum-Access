package com.example.quantumaccess.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.quantumaccess.ui.screens.BiometricLoginScreen
import com.example.quantumaccess.ui.screens.LocationVerificationScreen
import com.example.quantumaccess.ui.screens.RegisterScreen
import com.example.quantumaccess.ui.screens.SplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.LaunchedEffect
import com.example.quantumaccess.viewmodel.RegisterViewModel
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

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
            val vm: RegisterViewModel = viewModel()
            val context = LocalContext.current
            LaunchedEffect(Unit) {
                vm.events.collect { ev ->
                    when (ev) {
                        is RegisterViewModel.Event.Success -> {
                            navController.navigate(Routes.BiometricLogin) {
                                popUpTo(Routes.Register) { inclusive = true }
                            }
                        }
                        is RegisterViewModel.Event.Error -> {
                            Toast.makeText(context, ev.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            RegisterScreen(
                onRegister = { fullName, username, password, biometric ->
                    vm.register(fullName, username, password, biometric)
                },
                onLoginLink = { navController.navigate(Routes.BiometricLogin) }
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

