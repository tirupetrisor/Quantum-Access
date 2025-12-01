package com.example.quantumaccess.feature.location.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import com.example.quantumaccess.core.designsystem.theme.DeepBlue
import com.example.quantumaccess.core.designsystem.theme.SecureGreen

@Composable
fun StatusBadgeAuthorized(modifier: Modifier = Modifier) {
	Surface(
		shape = RoundedCornerShape(22.dp),
		shadowElevation = 6.dp,
		color = Color.White,
		modifier = modifier
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(22.dp))
				.padding(horizontal = 14.dp, vertical = 8.dp)
		) {
			Box(
				modifier = Modifier
					.size(10.dp)
					.background(SecureGreen, CircleShape)
			)
			Spacer(modifier = Modifier.size(8.dp))
			Text(text = "Authorized Access", color = DeepBlue, fontSize = 14.sp)
		}
	}
}

@Composable
fun StatusBadgeUnauthorized(modifier: Modifier = Modifier) {
	Surface(
		shape = RoundedCornerShape(22.dp),
		shadowElevation = 6.dp,
		color = Color.White,
		modifier = modifier
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(22.dp))
				.padding(horizontal = 14.dp, vertical = 8.dp)
		) {
			Box(
				modifier = Modifier
					.size(10.dp)
					.background(Color(0xFFD32F2F), CircleShape)
			)
			Spacer(modifier = Modifier.size(8.dp))
			Text(text = "Unauthorized Area", color = DeepBlue, fontSize = 14.sp)
		}
	}
}

@Composable
fun StatusBadgeUnknown(modifier: Modifier = Modifier) {
	Surface(
		shape = RoundedCornerShape(22.dp),
		shadowElevation = 6.dp,
		color = Color.White,
		modifier = modifier
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(22.dp))
				.padding(horizontal = 14.dp, vertical = 8.dp)
		) {
			Box(
				modifier = Modifier
					.size(10.dp)
					.background(Color(0xFF9E9E9E), CircleShape)
			)
			Spacer(modifier = Modifier.size(8.dp))
			Text(text = "Awaiting Verification", color = DeepBlue, fontSize = 14.sp)
		}
	}
}


