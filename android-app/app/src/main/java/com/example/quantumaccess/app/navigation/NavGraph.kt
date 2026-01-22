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
import com.example.quantumaccess.domain.model.TransactionScenario
import com.example.quantumaccess.viewmodel.LoginViewModel
import com.example.quantumaccess.viewmodel.RegisterViewModel
import kotlin.random.Random

@Composable
fun AppNavGraph() {
	val navController = rememberNavController()
    // Use Activity-scoped ViewModel for global actions like logout
    val globalLoginViewModel: LoginViewModel = viewModel(LocalContext.current as androidx.activity.ComponentActivity)

    val onLogoutAction: () -> Unit = {
        globalLoginViewModel.logout {
            navController.navigate(Routes.BiometricLogin) {
                popUpTo(Routes.Splash) { inclusive = false }
            }
        }
    }

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
                        is RegisterViewModel.Event.BiometricUpdated -> Unit
					}
				}
			}
			RegisterScreen(
                uiState = uiState,
				onRegister = { fullName, username, email, password, biometric ->
                    // Biometric logic deferred to Setup Screen
					vm.register(fullName, username, email, password, false)
				},
				onGoogleSignInSuccess = { email, name, googleId, biometricEnabled, idToken ->
					vm.onGoogleSignInSuccess(email, name, googleId, false, idToken)
				},
				onLoginLink = { navController.navigate(Routes.BiometricLogin) },
                onClearErrors = { vm.clearErrors() }
			)
		}
        composable(Routes.BiometricSetup) {
            val context = LocalContext.current
            val vm: RegisterViewModel = viewModel()

            LaunchedEffect(Unit) {
                vm.events.collect { event ->
                    when (event) {
                        is RegisterViewModel.Event.BiometricUpdated -> {
                            navController.navigate(Routes.LocationVerification) {
                                popUpTo(Routes.Register) { inclusive = true }
                                popUpTo(Routes.BiometricSetup) { inclusive = true }
                            }
                        }
                        is RegisterViewModel.Event.Error -> {
                            Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                }
            }
            
            BiometricSetupScreen(
                onEnable = { vm.enableBiometric(true) },
                onSkip = { vm.enableBiometric(false) }
            )
        }
		composable(Routes.BiometricLogin) {
            val context = LocalContext.current
            val prefs = com.example.quantumaccess.data.local.SecurePrefsManager(context)
            val isBiometricEnabled = prefs.isBiometricEnabled()
            val vm: LoginViewModel = viewModel()
            val uiState by vm.uiState.collectAsState()

            LaunchedEffect(Unit) {
                vm.events.collect { event ->
                    when (event) {
                        is LoginViewModel.LoginEvent.Success -> {
                            if (isBiometricEnabled) {
                                navController.navigate(Routes.LocationVerification) {
                                    popUpTo(Routes.BiometricLogin) { inclusive = true }
                                }
                            } else {
                                navController.navigate(Routes.BiometricSetup)
                            }
                        }
                        is LoginViewModel.LoginEvent.Error -> {
                            Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            
			BiometricLoginScreen(
                isLoading = uiState.isLoading,
				onAuthenticate = { vm.onBiometricAuthenticated() },
                onGoogleSignIn = { idToken ->
                    vm.onGoogleSignInSuccess(idToken)
                },
                onLoginWithPassword = { email, password ->
                     vm.login(email, password)
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
				onLogoutConfirm = onLogoutAction
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
				onLogout = onLogoutAction
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
				onLogout = onLogoutAction
			)
		}
		composable(Routes.TransactionMode) {
			InitiateTransactionScreen(
				onContinue = { amount, beneficiary, patientId, accessReason, mode, scenario, simulateAttack ->
					val amt = amount?.takeIf { it.isNotBlank() } ?: "0"
					val ben = beneficiary?.takeIf { it.isNotBlank() } ?: ""
					val pat = patientId?.takeIf { it.isNotBlank() } ?: ""
					val reason = accessReason?.takeIf { it.isNotBlank() } ?: ""
					when (mode) {
						TransactionMode.NORMAL -> {
							val route = "${Routes.NormalProcessing}?amount=${Uri.encode(amt)}&beneficiary=${Uri.encode(ben)}&patientId=${Uri.encode(pat)}&accessReason=${Uri.encode(reason)}&scenario=${scenario.name}&simulateAttack=$simulateAttack"
							navController.navigate(route)
						}
						TransactionMode.QUANTUM -> {
							val quantumId = generateQuantumId()
							val route = "${Routes.QuantumProcessing}?amount=${Uri.encode(amt)}&beneficiary=${Uri.encode(ben)}&patientId=${Uri.encode(pat)}&accessReason=${Uri.encode(reason)}&quantumId=${Uri.encode(quantumId)}&scenario=${scenario.name}&simulateAttack=$simulateAttack"
							navController.navigate(route)
						}
					}
				}
			)
		}
		composable(
			route = "${Routes.NormalProcessing}?amount={amount}&beneficiary={beneficiary}&patientId={patientId}&accessReason={accessReason}&scenario={scenario}&simulateAttack={simulateAttack}",
			arguments = listOf(
				navArgument("amount") { type = NavType.StringType; defaultValue = "0" },
				navArgument("beneficiary") { type = NavType.StringType; defaultValue = "" },
				navArgument("patientId") { type = NavType.StringType; defaultValue = "" },
				navArgument("accessReason") { type = NavType.StringType; defaultValue = "" },
				navArgument("scenario") { type = NavType.StringType; defaultValue = "BANKING_PAYMENT" },
				navArgument("simulateAttack") { type = NavType.BoolType; defaultValue = false }
			)
		) { backStackEntry ->
			val amountArg = backStackEntry.arguments?.getString("amount").orEmpty()
			val beneficiaryArg = backStackEntry.arguments?.getString("beneficiary").orEmpty()
			val patientIdArg = backStackEntry.arguments?.getString("patientId").orEmpty()
			val accessReasonArg = backStackEntry.arguments?.getString("accessReason").orEmpty()
			val scenarioArg = backStackEntry.arguments?.getString("scenario") ?: "BANKING_PAYMENT"
			val simulateAttackArg = backStackEntry.arguments?.getBoolean("simulateAttack") ?: false
			NormalTransactionProcessingScreen(
				amount = amountArg,
				beneficiary = beneficiaryArg,
				patientId = patientIdArg,
				accessReason = accessReasonArg,
				scenario = scenarioArg,
				simulateAttack = simulateAttackArg,
				onReturnToDashboard = {
					val popped = navController.popBackStack(Routes.Dashboard, inclusive = false)
					if (!popped) {
						navController.navigate(Routes.Dashboard) {
							popUpTo(Routes.Splash) { inclusive = false }
						}
					}
				},
				onLogout = onLogoutAction
			)
		}
		composable(
			route = "${Routes.QuantumProcessing}?amount={amount}&beneficiary={beneficiary}&patientId={patientId}&accessReason={accessReason}&quantumId={quantumId}&scenario={scenario}&simulateAttack={simulateAttack}",
			arguments = listOf(
				navArgument("amount") { type = NavType.StringType; defaultValue = "0" },
				navArgument("beneficiary") { type = NavType.StringType; defaultValue = "" },
				navArgument("patientId") { type = NavType.StringType; defaultValue = "" },
				navArgument("accessReason") { type = NavType.StringType; defaultValue = "" },
				navArgument("quantumId") { type = NavType.StringType; defaultValue = "#QTX-7F2A-8B91" },
				navArgument("scenario") { type = NavType.StringType; defaultValue = "BANKING_PAYMENT" },
				navArgument("simulateAttack") { type = NavType.BoolType; defaultValue = false }
			)
		) { backStackEntry ->
			val amountArg = backStackEntry.arguments?.getString("amount").orEmpty()
			val beneficiaryArg = backStackEntry.arguments?.getString("beneficiary").orEmpty()
			val patientIdArg = backStackEntry.arguments?.getString("patientId").orEmpty()
			val accessReasonArg = backStackEntry.arguments?.getString("accessReason").orEmpty()
			val quantumIdArg = backStackEntry.arguments?.getString("quantumId").orEmpty().ifBlank { "#QTX-7F2A-8B91" }
			val scenarioArg = backStackEntry.arguments?.getString("scenario") ?: "BANKING_PAYMENT"
			val simulateAttackArg = backStackEntry.arguments?.getBoolean("simulateAttack") ?: false
			QuantumTransactionProcessingScreen(
				amount = amountArg,
				beneficiary = beneficiaryArg,
				patientId = patientIdArg,
				accessReason = accessReasonArg,
				quantumId = quantumIdArg,
				scenario = scenarioArg,
				simulateAttack = simulateAttackArg,
				onReturnToDashboard = {
					val popped = navController.popBackStack(Routes.Dashboard, inclusive = false)
					if (!popped) {
						navController.navigate(Routes.Dashboard) {
							popUpTo(Routes.Splash) { inclusive = false }
						}
					}
				},
				onLogout = onLogoutAction
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
