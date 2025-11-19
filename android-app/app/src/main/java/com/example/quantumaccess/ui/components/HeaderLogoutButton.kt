package com.example.quantumaccess.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reusable circular logout icon button suitable for header bars.
 */
@Composable
fun HeaderLogoutButton(
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	backgroundColor: Color = Color.White.copy(alpha = 0.18f),
	iconTint: Color = Color.White,
	size: Dp = 40.dp,
	iconSize: Dp = 18.dp
) {
	Box(
		modifier = modifier
			.size(size)
			.clip(CircleShape)
			.background(backgroundColor)
			.clickable { onClick() },
		contentAlignment = Alignment.Center
	) {
		Icon(
			imageVector = Icons.Filled.PowerSettingsNew,
			contentDescription = "Logout",
			tint = iconTint,
			modifier = Modifier.size(iconSize)
		)
	}
}


