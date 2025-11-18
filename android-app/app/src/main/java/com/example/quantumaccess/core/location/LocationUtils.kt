package com.example.quantumaccess.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale

const val TIMISOARA_LAT = 45.7489
const val TIMISOARA_LON = 21.2087
const val GEOFENCE_RADIUS_METERS = 15000.0 // 15 km

fun hasFineLocationPermission(context: Context): Boolean {
	return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
}

@SuppressLint("MissingPermission")
fun verifyWithCurrentLocation(context: Context): Pair<Boolean, Double?> {
	val locMan = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
	val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
	val last: List<Location> = providers.mapNotNull { prov ->
		runCatching { locMan.getLastKnownLocation(prov) }.getOrNull()
	}
	val best = last.maxByOrNull { it.time } ?: return Pair(false, null)
	val dist = haversineMeters(best.latitude, best.longitude, TIMISOARA_LAT, TIMISOARA_LON)
	return Pair(dist <= GEOFENCE_RADIUS_METERS, dist)
}

@SuppressLint("MissingPermission")
suspend fun fetchFreshLocation(context: Context): Location? {
	val fused = LocationServices.getFusedLocationProviderClient(context)
	return suspendCancellableCoroutine { cont ->
		val cts = CancellationTokenSource()
		fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
			.addOnSuccessListener { loc ->
				if (!cont.isCompleted) cont.resume(loc, onCancellation = null)
			}
			.addOnFailureListener {
				if (!cont.isCompleted) cont.resume(null, onCancellation = null)
			}
		cont.invokeOnCancellation { cts.cancel() }
	}
}

fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
	val R = 6371000.0
	val dLat = Math.toRadians(lat2 - lat1)
	val dLon = Math.toRadians(lon2 - lon1)
	val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
			Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
			Math.sin(dLon / 2) * Math.sin(dLon / 2)
	val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
	return R * c
}

fun geocodeToLatLon(context: Context, query: String): Pair<Double, Double>? {
	return runCatching {
		val geocoder = Geocoder(context, Locale.getDefault())
		val results = geocoder.getFromLocationName(query, 1)
		if (results.isNullOrEmpty()) null
		else Pair(results[0].latitude, results[0].longitude)
	}.getOrNull()
}


