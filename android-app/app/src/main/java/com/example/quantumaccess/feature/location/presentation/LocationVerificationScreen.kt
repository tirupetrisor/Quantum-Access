package com.example.quantumaccess.feature.location.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.quantumaccess.core.location.GEOFENCE_RADIUS_METERS
import com.example.quantumaccess.core.location.TIMISOARA_LAT
import com.example.quantumaccess.core.location.TIMISOARA_LON
import com.example.quantumaccess.core.location.fetchFreshLocation
import com.example.quantumaccess.core.location.haversineMeters
import com.example.quantumaccess.core.location.verifyWithCurrentLocation
import com.example.quantumaccess.core.designsystem.components.PrimaryActionButton
import com.example.quantumaccess.core.designsystem.components.QuantumLogo
import com.example.quantumaccess.core.designsystem.theme.BorderLight
import com.example.quantumaccess.core.designsystem.theme.DeepBlue
import com.example.quantumaccess.core.designsystem.theme.NightBlack
import com.example.quantumaccess.core.designsystem.theme.Slate800
import com.example.quantumaccess.feature.location.presentation.components.ManualLocationDialog
import com.example.quantumaccess.feature.location.presentation.components.MapVisual
import com.example.quantumaccess.feature.location.presentation.components.StatusBadgeAuthorized
import com.example.quantumaccess.feature.location.presentation.components.StatusBadgeUnauthorized
import com.example.quantumaccess.feature.location.presentation.components.StatusBadgeUnknown
import kotlinx.coroutines.launch
import java.util.Locale

import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat
import androidx.compose.ui.platform.LocalView
import android.app.Activity
import com.example.quantumaccess.core.util.findActivity

@Composable
fun LocationVerificationScreen(
	modifier: Modifier = Modifier,
	onUseCurrentLocation: () -> Unit = {},
	onEnterLocationManually: () -> Unit = {},
	onContinueToDashboard: () -> Unit = {}
) {
	val context = LocalContext.current
	var authorized by remember { mutableStateOf<Boolean?>(null) }
	var distanceMeters by remember { mutableStateOf<Double?>(null) }
	var showManual by remember { mutableStateOf(false) }
	val scope = rememberCoroutineScope()

    // Force status bar icons to be dark (visible on white background) for this screen
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = context.findActivity()?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                // Set to true (dark icons) for this screen
                controller.isAppearanceLightStatusBars = true
            }
        }
    }

	val permissionGranted = remember {
		ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
	}
	var hasPermission by remember { mutableStateOf(permissionGranted) }

	val requestPermission = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.RequestPermission(),
		onResult = { granted ->
			hasPermission = granted
			if (granted) {
				val (auth, dist) = verifyWithCurrentLocation(context)
				authorized = auth
				distanceMeters = dist
			}
		}
	)

	// Default: unknown until user acts
	LaunchedEffect(Unit) {
		authorized = null
		distanceMeters = null
	}

	Box(
		modifier = modifier
			.fillMaxSize()
			.background(Color.White)
            .systemBarsPadding()
			.padding(horizontal = 24.dp)
			.widthIn(max = 480.dp),
		contentAlignment = Alignment.TopCenter
	) {
		Column(
            horizontalAlignment = Alignment.CenterHorizontally, 
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp)
        ) {
            // Header-like logo at the top
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                QuantumLogo(
                    iconSize = 32.dp,
                    showText = true
                )
            }

			Spacer(modifier = Modifier.height(12.dp))

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.Center,
				verticalAlignment = Alignment.CenterVertically
			) {
				when (authorized) {
					true -> StatusBadgeAuthorized(modifier = Modifier.align(Alignment.CenterVertically))
					false -> StatusBadgeUnauthorized(modifier = Modifier.align(Alignment.CenterVertically))
					null -> StatusBadgeUnknown(modifier = Modifier.align(Alignment.CenterVertically))
				}
			}

			Spacer(modifier = Modifier.height(16.dp))

			Surface(
				shape = RoundedCornerShape(24.dp),
				shadowElevation = 10.dp,
				color = Color.White,
				modifier = Modifier
					.fillMaxWidth()
					.border(1.dp, BorderLight, RoundedCornerShape(24.dp))
			) {
				Column(modifier = Modifier.padding(16.dp)) {
					MapVisual(
						authorized = authorized,
						modifier = Modifier
						.fillMaxWidth()
						.height(220.dp) // Reduced height for better fit
						.clip(RoundedCornerShape(16.dp))
						.background(Color(0xFFF8FAFC))
					)

					Spacer(modifier = Modifier.height(16.dp))

					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.Center,
						verticalAlignment = Alignment.CenterVertically
					) {
						Icon(
							imageVector = Icons.Filled.LocationOn,
							contentDescription = null,
							tint = DeepBlue,
							modifier = Modifier.size(18.dp)
						)
						Spacer(modifier = Modifier.size(8.dp))
						Text(
							text = "Location validated via GPS",
							color = DeepBlue,
							style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
						)
					}
					Spacer(modifier = Modifier.height(6.dp))
					if (distanceMeters != null) {
						val km = (distanceMeters ?: 0.0) / 1000.0
						Text(
							text = String.format(Locale.getDefault(), "Distance to Timișoara center: %.1f km", km),
							color = Slate800,
							style = MaterialTheme.typography.bodySmall,
							modifier = Modifier.fillMaxWidth(),
							textAlign = TextAlign.Center
						)
					} else {
						Text(
							text = "Tap a button below to verify your location",
							color = Slate800,
							style = MaterialTheme.typography.bodySmall,
							modifier = Modifier.fillMaxWidth(),
							textAlign = TextAlign.Center
						)
					}

					Spacer(modifier = Modifier.height(16.dp))

					PrimaryActionButton(
						text = "Use Current Location",
						onClick = {
							if (!hasPermission) {
								requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
							} else {
								scope.launch {
									val loc = fetchFreshLocation(context)
									if (loc != null) {
										val dist = haversineMeters(loc.latitude, loc.longitude, TIMISOARA_LAT, TIMISOARA_LON)
										distanceMeters = dist
										authorized = dist <= GEOFENCE_RADIUS_METERS
									} else {
										// fallback to last known if current not available
										val (auth, dist) = verifyWithCurrentLocation(context)
										authorized = auth
										distanceMeters = dist
									}
								}
							}
							onUseCurrentLocation()
						},
						leadingIcon = {
							Icon(
								imageVector = Icons.Filled.MyLocation,
								contentDescription = null,
								modifier = Modifier.size(18.dp),
								tint = Color.White
							)
						}
					)
					Spacer(modifier = Modifier.height(10.dp))
					OutlinedButton(
						onClick = {
							showManual = true
							onEnterLocationManually()
						},
						shape = RoundedCornerShape(16.dp),
						border = BorderStroke(1.dp, DeepBlue),
						colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepBlue),
						modifier = Modifier.fillMaxWidth()
					) {
						Icon(imageVector = Icons.Filled.Keyboard, contentDescription = null, modifier = Modifier.size(18.dp), tint = DeepBlue)
						Spacer(modifier = Modifier.size(8.dp))
						Text(text = "Enter Location Manually", color = DeepBlue)
					}

					if (showManual) {
						ManualLocationDialog(
							onResolvedLocation = { lat, lon ->
								scope.launch {
									val dist = haversineMeters(lat, lon, TIMISOARA_LAT, TIMISOARA_LON)
									distanceMeters = dist
									authorized = dist <= GEOFENCE_RADIUS_METERS
									showManual = false
								}
							},
							onDismiss = { showManual = false }
						)
					}

					Spacer(modifier = Modifier.height(12.dp))
					PrimaryActionButton(
						text = "Continue",
						onClick = { onContinueToDashboard() },
						enabled = authorized == true
					)
				}
			}
		}
	}
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LocationVerificationPreview() {
	LocationVerificationScreen()
}



