package com.example.quantumaccess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.quantumaccess.ui.components.HeaderLogoutButton
import com.example.quantumaccess.ui.components.QuantumLogoBlueOrbits
import com.example.quantumaccess.ui.theme.DeepBlue

@Composable
fun DashboardScreen(
	modifier: Modifier = Modifier,
	onInitiateTransaction: () -> Unit = {},
	onOpenHistory: () -> Unit = {},
	onOpenAnalytics: () -> Unit = {},
	onLogoutConfirm: () -> Unit = {}
) {
	var showLogout by remember { mutableStateOf(false) }
	Box(
		modifier = modifier
			.fillMaxSize()
			.background(Color.White)
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
		) {
			Header(onLogout = { showLogout = true })
			Spacer(modifier = Modifier.height(16.dp))
			Column(
				modifier = Modifier
					.padding(horizontal = 20.dp)
					.weight(1f),
				verticalArrangement = Arrangement.Center
			) {
				DashboardCard(
					title = "Initiate Transaction",
					subtitle = "Start a new secure payment",
					leading = {
						IconBubble(iconTint = DeepBlue, background = DeepBlue.copy(alpha = 0.08f)) {
							Icon(imageVector = Icons.Filled.Security, contentDescription = null, tint = DeepBlue)
						}
					},
					onClick = onInitiateTransaction,
					borderColor = DeepBlue
				)
				Spacer(modifier = Modifier.height(16.dp))
				DashboardCard(
					title = "Transaction History",
					subtitle = "View your recent operations",
					leading = {
						IconBubble(iconTint = Color(0xFF4B5563), background = Color(0xFFF3F4F6)) {
							Icon(imageVector = Icons.Filled.History, contentDescription = null, tint = Color(0xFF4B5563))
						}
					},
					onClick = onOpenHistory,
					borderColor = Color(0xFFE5E7EB)
				)
				Spacer(modifier = Modifier.height(16.dp))
				DashboardCard(
					title = "Security Analytics",
					subtitle = "See insights on Quantum vs\nNormal transactions",
					leading = {
						IconBubble(iconTint = Color(0xFF4B5563), background = Color(0xFFF3F4F6)) {
							Icon(imageVector = Icons.Filled.ShowChart, contentDescription = null, tint = Color(0xFF4B5563))
						}
					},
					onClick = onOpenAnalytics,
					borderColor = Color(0xFFE5E7EB)
				)
			}
			Footer()
		}

		if (showLogout) {
			LogoutDialog(
				onCancel = { showLogout = false },
				onConfirm = {
					showLogout = false
					onLogoutConfirm()
				}
			)
		}
	}
}

@Composable
private fun Header(onLogout: () -> Unit) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.background(DeepBlue)
			.padding(horizontal = 16.dp, vertical = 14.dp)
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween,
			modifier = Modifier.fillMaxWidth()
		) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				Box(
					modifier = Modifier
						.size(36.dp)
						.clip(RoundedCornerShape(10.dp))
						.background(Color.White),
					contentAlignment = Alignment.Center
				) {
					QuantumLogoBlueOrbits(showText = false, iconSize = 24.dp)
				}
				Spacer(modifier = Modifier.width(10.dp))
				Column {
					Text("QuantumAccess", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
					Text("Dashboard", color = Color(0xFFCBD5E1), style = MaterialTheme.typography.labelSmall)
				}
			}
			HeaderLogoutButton(onClick = onLogout)
		}
	}
}

@Composable
private fun LogoutDialog(onCancel: () -> Unit, onConfirm: () -> Unit) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(Color.Black.copy(alpha = 0.5f)),
		contentAlignment = Alignment.Center
	) {
		Surface(
			shape = RoundedCornerShape(16.dp),
			shadowElevation = 10.dp,
			color = Color.White,
			modifier = Modifier
				.padding(horizontal = 20.dp)
		) {
			Column(modifier = Modifier.padding(16.dp)) {
				Text("Log Out", style = MaterialTheme.typography.titleMedium, color = Color(0xFF111827), fontWeight = FontWeight.SemiBold)
				Spacer(modifier = Modifier.height(8.dp))
				Text("Are you sure you want to log out?", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280))
				Spacer(modifier = Modifier.height(16.dp))
				Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
					Surface(
						shape = RoundedCornerShape(12.dp),
						color = Color(0xFFF3F4F6),
						modifier = Modifier
							.weight(1f)
							.clip(RoundedCornerShape(12.dp))
							.clickable { onCancel() }
					) {
						Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
							Text("Cancel", color = Color(0xFF374151), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
						}
					}
					Surface(
						shape = RoundedCornerShape(12.dp),
						color = DeepBlue,
						modifier = Modifier
							.weight(1f)
							.clip(RoundedCornerShape(12.dp))
							.clickable { onConfirm() }
					) {
						Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
							Text("Confirm", color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
						}
					}
				}
			}
		}
	}
}

@Composable
private fun DashboardCard(
	title: String,
	subtitle: String,
	leading: @Composable () -> Unit,
	onClick: () -> Unit,
	borderColor: Color
) {
	Surface(
		shape = RoundedCornerShape(18.dp),
		shadowElevation = 6.dp,
		color = Color.White,
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(18.dp))
			.clickable { onClick() },
		tonalElevation = 0.dp,
		border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween,
			modifier = Modifier
				.fillMaxWidth()
				.padding(18.dp)
		) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				leading()
				Spacer(modifier = Modifier.width(14.dp))
				Column {
					Text(title, style = MaterialTheme.typography.titleMedium, color = Color(0xFF111827), fontWeight = FontWeight.Medium)
					Spacer(modifier = Modifier.height(4.dp))
					Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280))
				}
			}
			Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFF9CA3AF))
		}
	}
}

@Composable
private fun IconBubble(
	iconTint: Color,
	background: Color,
	content: @Composable () -> Unit
) {
	Box(
		modifier = Modifier
			.size(44.dp)
			.clip(CircleShape)
			.background(background),
		contentAlignment = Alignment.Center
	) {
		content()
	}
}

@Composable
private fun Footer() {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(bottom = 16.dp, top = 8.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			Box(
				modifier = Modifier
					.size(8.dp)
					.clip(CircleShape)
					.background(Color(0xFF22C55E))
			)
			Spacer(modifier = Modifier.width(8.dp))
			Text("Quantum Network Active", color = Color(0xFF6B7280), style = MaterialTheme.typography.labelSmall)
		}
		Spacer(modifier = Modifier.height(6.dp))
		Text(
			"Secured by Quantum Key Distribution",
			color = Color(0xFF94A3B8),
			style = MaterialTheme.typography.labelSmall,
			textAlign = TextAlign.Center
		)
		Spacer(modifier = Modifier.height(8.dp))
	}
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DashboardPreview() {
	Surface(color = Color.White) {
		Column {
			DashboardScreen()
		}
	}
}


