package com.example.quantumaccess.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.location.Geocoder
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import java.util.Locale
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.example.quantumaccess.ui.components.QuantumLogo
import com.example.quantumaccess.ui.theme.DeepBlue
import com.example.quantumaccess.ui.theme.SecureGreen
import androidx.compose.material3.OutlinedTextField
import com.example.quantumaccess.ui.location.MapVisual
import com.example.quantumaccess.ui.location.ManualLocationDialog
import com.example.quantumaccess.ui.location.StatusBadgeAuthorized
import com.example.quantumaccess.ui.location.StatusBadgeUnauthorized
import com.example.quantumaccess.ui.location.StatusBadgeUnknown
import com.example.quantumaccess.core.location.fetchFreshLocation
import com.example.quantumaccess.core.location.haversineMeters
import com.example.quantumaccess.core.location.verifyWithCurrentLocation
import com.example.quantumaccess.core.location.GEOFENCE_RADIUS_METERS
import com.example.quantumaccess.core.location.TIMISOARA_LAT
import com.example.quantumaccess.core.location.TIMISOARA_LON

@Composable
fun LocationVerificationScreen(
	modifier: Modifier = Modifier,
	onUseCurrentLocation: () -> Unit = {},
	onEnterLocationManually: () -> Unit = {}
) {
	val context = LocalContext.current
	var authorized by remember { mutableStateOf<Boolean?>(null) }
	var distanceMeters by remember { mutableStateOf<Double?>(null) }
	var showManual by remember { mutableStateOf(false) }
	val scope = rememberCoroutineScope()

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
			.padding(horizontal = 24.dp, vertical = 16.dp)
			.widthIn(max = 480.dp),
		contentAlignment = Alignment.Center
	) {
		// Header-like logo at the top-left
		QuantumLogo(
			iconSize = 32.dp,
			showText = true,
			modifier = Modifier
				.align(Alignment.TopStart)
				.padding(top = 35.dp)
		)

		// Place the content slightly below exact center
		Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 24.dp)) {
			Spacer(modifier = Modifier.height(25.dp))

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
					.border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(24.dp))
			) {
				Column(modifier = Modifier.padding(16.dp)) {
					MapVisual(
						authorized = authorized,
						modifier = Modifier
						.fillMaxWidth()
						.height(300.dp)
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
							color = Color(0xFF6B7280),
							style = MaterialTheme.typography.bodySmall,
							modifier = Modifier.fillMaxWidth(),
							textAlign = TextAlign.Center
						)
					} else {
						Text(
							text = "Tap a button below to verify your location",
							color = Color(0xFF6B7280),
							style = MaterialTheme.typography.bodySmall,
							modifier = Modifier.fillMaxWidth(),
							textAlign = TextAlign.Center
						)
					}

					Spacer(modifier = Modifier.height(16.dp))

					Button(
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
						colors = ButtonDefaults.buttonColors(containerColor = DeepBlue, contentColor = Color.White),
						shape = RoundedCornerShape(16.dp),
						modifier = Modifier.fillMaxWidth()
					) {
						Icon(imageVector = Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
						Spacer(modifier = Modifier.size(8.dp))
						Text(text = "Use Current Location")
					}
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



