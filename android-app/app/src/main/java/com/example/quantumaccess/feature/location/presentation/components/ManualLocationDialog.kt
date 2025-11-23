package com.example.quantumaccess.feature.location.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quantumaccess.core.designsystem.theme.DeepBlue
import com.example.quantumaccess.core.designsystem.theme.SecureGreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.quantumaccess.core.location.geocodeToLatLon

@Composable
fun ManualLocationDialog(
	onResolvedLocation: (Double, Double) -> Unit,
	onDismiss: () -> Unit
) {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()
	var query by remember { mutableStateOf("") }
	var error by remember { mutableStateOf<String?>(null) }
	var inProgress by remember { mutableStateOf(false) }

	Surface(
		shape = RoundedCornerShape(16.dp),
		shadowElevation = 12.dp,
		color = Color.White,
		modifier = Modifier
			.fillMaxWidth()
			.padding(top = 8.dp)
	) {
		Column(modifier = Modifier.padding(16.dp)) {
			Text("Enter your location (address or city)", fontWeight = FontWeight.SemiBold, color = DeepBlue)
			Spacer(modifier = Modifier.height(12.dp))
			OutlinedTextField(
				value = query,
				onValueChange = { query = it; error = null },
				singleLine = true,
				label = { Text("Location") },
				modifier = Modifier.fillMaxWidth()
			)
			if (error != null) {
				Spacer(modifier = Modifier.height(6.dp))
				Text(text = error ?: "", color = Color(0xFFD32F2F), fontSize = 12.sp)
			}
			Spacer(modifier = Modifier.height(12.dp))
			Button(
				onClick = {
					if (query.isBlank()) {
						error = "Please type a location"
						return@Button
					}
					inProgress = true
					scope.launch {
						val coords = withContext(Dispatchers.IO) { geocodeToLatLon(context, query) }
						inProgress = false
						if (coords == null) {
							error = "Location not found"
						} else {
							onResolvedLocation(coords.first, coords.second)
						}
					}
				},
				enabled = !inProgress,
				colors = ButtonDefaults.buttonColors(containerColor = SecureGreen, contentColor = Color.White),
				shape = RoundedCornerShape(12.dp),
				modifier = Modifier.fillMaxWidth()
			) { Text(if (inProgress) "Resolving..." else "Validate") }
			Spacer(modifier = Modifier.height(8.dp))
			OutlinedButton(
				onClick = onDismiss,
				shape = RoundedCornerShape(12.dp),
				border = BorderStroke(1.dp, DeepBlue),
				colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepBlue),
				modifier = Modifier.fillMaxWidth()
			) { Text("Cancel", color = DeepBlue) }
		}
	}
}


