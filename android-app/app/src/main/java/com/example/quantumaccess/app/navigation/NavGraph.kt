package com.example.quantumaccess.app.navigation

import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.quantumaccess.feature.analytics.presentation.AnalyticsDashboardScreen
import com.example.quantumaccess.feature.auth.presentation.BiometricLoginScreen
import com.example.quantumaccess.feature.auth.presentation.BiometricSetupScreen
import com.example.quantumaccess.feature.auth.presentation.RegisterScreen
import com.example.quantumaccess.feature.dashboard.presentation.DashboardScreen
import com.example.quantumaccess.feature.history.presentation.TransactionHistoryScreen
import com.example.quantumaccess.feature.location.presentation.LocationVerificationScreen
import com.example.quantumaccess.feature.splash.presentation.SplashScreen
import com.example.quantumaccess.feature.transactions.presentation.InitiateTransactionScreen
import com.example.quantumaccess.feature.transactions.presentation.NormalTransactionProcessingScreen
import com.example.quantumaccess.feature.transactions.presentation.QuantumTransactionProcessingScreen
import com.example.quantumaccess.feature.transactions.presentation.TransactionMode
import com.example.quantumaccess.viewmodel.RegisterViewModel
import kotlin.random.Random

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
            val uiState by vm.uiState.collectAsState()
            
			LaunchedEffect(Unit) {
				vm.events.collect { ev ->
					when (ev) {
						is RegisterViewModel.Event.Success -> {
							navController.navigate(Routes.BiometricSetup) {
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
                uiState = uiState,
				onRegister = { fullName, username, email, password, biometric ->
                    // We ignore biometric checkbox from RegisterScreen if we are moving to Setup Screen, 
                    // OR we pass it. The user prompt says "Register & Continue -> Biometric Setup".
                    // So we should probably disable biometric here or assume false and let Setup handle it.
                    // However, the prompt said "Biometric Setup Screen (dacă login cu Google) OR (Enable fingerprint?)".
                    // Let's stick to: Register -> Success -> Setup Screen.
                    // So we pass biometricEnabled=false initially to repo (or we can pass it and if true, skip setup? No, prompts asks for Setup Screen).
                    // Let's assume we pass false here to force the setup screen flow, OR we use the existing logic but navigate to Setup instead of Login.
                    
					vm.register(fullName, username, email, password, false)
				},
				onGoogleSignInSuccess = { email, name, googleId, biometricEnabled ->
                    // Ignore checkbox for Google too, go to setup
					vm.onGoogleSignInSuccess(email, name, googleId, false)
				},
				onLoginLink = { navController.navigate(Routes.BiometricLogin) },
                onClearErrors = { vm.clearErrors() }
			)
		}
        composable(Routes.BiometricSetup) {
            val context = LocalContext.current
            // In a real app, we'd use a ViewModel to enable biometric for the current user.
            // For now, we can use SecurePrefsManager directly or a ViewModel.
            // Since we don't have a BiometricViewModel, we'll use a simple side effect or assume the user is "logged in" in memory.
            // But wait, we need to update the DB or Prefs for the CURRENT user.
            // The RegisterViewModel just registered them.
            // For simplicity in this "mock" flow without a SessionManager:
            // We'll just set the global preference.
            
            val prefs = com.example.quantumaccess.data.local.SecurePrefsManager(context)
            
            BiometricSetupScreen(
                onEnable = {
                    prefs.setBiometricEnabled(true)
                    navController.navigate(Routes.LocationVerification) {
                        popUpTo(Routes.Register) { inclusive = true }
                        popUpTo(Routes.BiometricSetup) { inclusive = true }
                    }
                },
                onSkip = {
                    prefs.setBiometricEnabled(false)
                    navController.navigate(Routes.LocationVerification) {
                         popUpTo(Routes.Register) { inclusive = true }
                         popUpTo(Routes.BiometricSetup) { inclusive = true }
                    }
                }
            )
        }
		composable(Routes.BiometricLogin) {
            val context = LocalContext.current
            val prefs = com.example.quantumaccess.data.local.SecurePrefsManager(context)
            val isBiometricEnabled = prefs.isBiometricEnabled()
            
			BiometricLoginScreen(
				onAuthenticate = {
                    // Success -> Dashboard
					navController.navigate(Routes.LocationVerification) {
						popUpTo(Routes.BiometricLogin) { inclusive = true }
					}
				},
                onLoginWithPassword = { _, _ ->
                     // Login success -> Check if biometric enabled
                     if (isBiometricEnabled) {
                         navController.navigate(Routes.LocationVerification) {
                            popUpTo(Routes.BiometricLogin) { inclusive = true }
                         }
                     } else {
                         // If not enabled, offer setup
                         navController.navigate(Routes.BiometricSetup)
                     }
                }
			)
		}
		composable(Routes.LocationVerification) {
			LocationVerificationScreen(
				onContinueToDashboard = {
					navController.navigate(Routes.Dashboard) {
						popUpTo(Routes.LocationVerification) { inclusive = true }
					}
				}
			)
		}
		composable(Routes.Dashboard) {
			DashboardScreen(
				onInitiateTransaction = {
					navController.navigate(Routes.TransactionMode)
				},
				onOpenHistory = { navController.navigate(Routes.TransactionHistory) },
				onOpenAnalytics = { navController.navigate(Routes.Analytics) },
				onLogoutConfirm = {
					navController.navigate(Routes.BiometricLogin) {
						popUpTo(Routes.Splash) { inclusive = false }
					}
				}
			)
		}
		composable(Routes.TransactionHistory) {
			val context = LocalContext.current
			TransactionHistoryScreen(
				onReturnToDashboard = {
					val popped = navController.popBackStack(Routes.Dashboard, inclusive = false)
					if (!popped) {
						navController.navigate(Routes.Dashboard) {
							popUpTo(Routes.Splash) { inclusive = false }
						}
					}
				},
				onLoadMore = {
					Toast.makeText(context, "Loading more transactions soon", Toast.LENGTH_SHORT).show()
				},
				onLogout = {
					navController.navigate(Routes.BiometricLogin) {
						popUpTo(Routes.Splash) { inclusive = false }
					}
				}
			)
		}
		composable(Routes.Analytics) {
			AnalyticsDashboardScreen(
				onReturnToDashboard = {
					val popped = navController.popBackStack(Routes.Dashboard, inclusive = false)
					if (!popped) {
						navController.navigate(Routes.Dashboard) {
							popUpTo(Routes.Splash) { inclusive = false }
						}
					}
				},
				onLogout = {
					navController.navigate(Routes.BiometricLogin) {
						popUpTo(Routes.Splash) { inclusive = false }
					}
				}
			)
		}
		composable(Routes.TransactionMode) {
			InitiateTransactionScreen(
				onContinue = { amount, beneficiary, mode ->
					when (mode) {
						TransactionMode.NORMAL -> {
							val normalizedAmount = amount.ifBlank { "€1,250.00" }
							val normalizedBeneficiary = beneficiary.ifBlank { "John D. – Quantum Savings" }
							val route =
								"${Routes.NormalProcessing}?amount=${Uri.encode(normalizedAmount)}&beneficiary=${Uri.encode(normalizedBeneficiary)}"
							navController.navigate(route)
						}
						TransactionMode.QUANTUM -> {
							val normalizedAmount = amount.ifBlank { "€2,450.00" }
							val normalizedBeneficiary = beneficiary.ifBlank { "TechCorp Solutions SRL" }
							val quantumId = generateQuantumId()
							val route =
								"${Routes.QuantumProcessing}?amount=${Uri.encode(normalizedAmount)}&beneficiary=${Uri.encode(normalizedBeneficiary)}&quantumId=${Uri.encode(quantumId)}"
							navController.navigate(route)
						}
					}
				}
			)
		}
		composable(
			route = "${Routes.NormalProcessing}?amount={amount}&beneficiary={beneficiary}",
			arguments = listOf(
				navArgument("amount") {
					type = NavType.StringType
					defaultValue = "€1,250.00"
				},
				navArgument("beneficiary") {
					type = NavType.StringType
					defaultValue = "John D. – Quantum Savings"
				}
			)
		) { backStackEntry ->
			val amountArg = backStackEntry.arguments?.getString("amount").orEmpty().ifBlank { "€1,250.00" }
			val beneficiaryArg =
				backStackEntry.arguments?.getString("beneficiary").orEmpty().ifBlank { "John D. – Quantum Savings" }
			NormalTransactionProcessingScreen(
				amount = amountArg,
				beneficiary = beneficiaryArg,
				onReturnToDashboard = {
					val popped = navController.popBackStack(Routes.Dashboard, inclusive = false)
					if (!popped) {
						navController.navigate(Routes.Dashboard) {
							popUpTo(Routes.Splash) { inclusive = false }
						}
					}
				},
				onLogout = {
					navController.navigate(Routes.BiometricLogin) {
						popUpTo(Routes.Splash) { inclusive = false }
					}
				}
			)
		}
		composable(
			route = "${Routes.QuantumProcessing}?amount={amount}&beneficiary={beneficiary}&quantumId={quantumId}",
			arguments = listOf(
				navArgument("amount") {
					type = NavType.StringType
					defaultValue = "€2,450.00"
				},
				navArgument("beneficiary") {
					type = NavType.StringType
					defaultValue = "TechCorp Solutions SRL"
				},
				navArgument("quantumId") {
					type = NavType.StringType
					defaultValue = "#QTX-7F2A-8B91"
				}
			)
		) { backStackEntry ->
			val amountArg = backStackEntry.arguments?.getString("amount").orEmpty().ifBlank { "€2,450.00" }
			val beneficiaryArg =
				backStackEntry.arguments?.getString("beneficiary").orEmpty().ifBlank { "TechCorp Solutions SRL" }
			val quantumIdArg =
				backStackEntry.arguments?.getString("quantumId").orEmpty().ifBlank { "#QTX-7F2A-8B91" }
			QuantumTransactionProcessingScreen(
				amount = amountArg,
				beneficiary = beneficiaryArg,
				quantumId = quantumIdArg,
				onReturnToDashboard = {
					val popped = navController.popBackStack(Routes.Dashboard, inclusive = false)
					if (!popped) {
						navController.navigate(Routes.Dashboard) {
							popUpTo(Routes.Splash) { inclusive = false }
						}
					}
				},
				onLogout = {
					navController.navigate(Routes.BiometricLogin) {
						popUpTo(Routes.Splash) { inclusive = false }
					}
				}
			)
		}
	}
}

private fun generateQuantumId(): String {
	val alphabet = "0123456789ABCDEF"
	fun segment(length: Int) = buildString {
		repeat(length) {
			append(alphabet[Random.nextInt(alphabet.length)])
		}
	}
	return "#QTX-${segment(4)}-${segment(4)}"
}
