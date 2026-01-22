package com.example.quantumaccess.feature.dashboard.presentation

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.quantumaccess.core.designsystem.components.QuantumTopBar
import com.example.quantumaccess.core.designsystem.theme.BorderLight
import com.example.quantumaccess.core.designsystem.theme.Cloud200
import com.example.quantumaccess.core.designsystem.theme.DeepBlue
import com.example.quantumaccess.core.designsystem.theme.Emerald
import com.example.quantumaccess.core.designsystem.theme.NightBlack
import com.example.quantumaccess.core.designsystem.theme.Slate700
import com.example.quantumaccess.core.designsystem.theme.Slate800
import com.example.quantumaccess.core.designsystem.theme.Steel200
import com.example.quantumaccess.core.designsystem.theme.Steel300
import com.example.quantumaccess.core.designsystem.theme.Steel300 as Steel300Color
import com.example.quantumaccess.data.sample.RepositoryProvider
import com.example.quantumaccess.domain.model.SecurityScoreSummary
import com.example.quantumaccess.domain.repository.TransactionRepository

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity
import com.example.quantumaccess.core.util.findActivity

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun DashboardScreen(
	modifier: Modifier = Modifier,
	onInitiateTransaction: () -> Unit = {},
	onOpenHistory: () -> Unit = {},
	onOpenAnalytics: () -> Unit = {},
	onLogoutConfirm: () -> Unit = {},
	transactionRepository: TransactionRepository = RepositoryProvider.transactionRepository
) {
    // Force status bar icons to be light (visible on blue background)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
            }
        }
    }
    
    // Get security score summary
    val scoreSummary = remember(transactionRepository) { 
        transactionRepository.getSecurityScoreSummary() 
    }

	Box(
		modifier = modifier
			.fillMaxSize()
			.background(Color.White)
            .navigationBarsPadding()
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
		) {
			QuantumTopBar(
				title = "QuantumAccess",
				subtitle = "Dashboard",
				showLogoutButton = true,
				onLogoutClick = onLogoutConfirm
			)
			Column(
				modifier = Modifier
					.padding(horizontal = 20.dp)
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
				verticalArrangement = Arrangement.Top
			) {
				Spacer(modifier = Modifier.height(16.dp))
				
				// Security Score Card
				SecurityScoreCard(scoreSummary = scoreSummary)
				
				Spacer(modifier = Modifier.height(20.dp))
				
				DashboardCard(
					title = "Initiate Transaction",
					subtitle = "Start a new secure payment",
					leading = {
						IconBubble(iconTint = DeepBlue, background = DeepBlue.copy(alpha = 0.08f)) {
							Icon(imageVector = Icons.Filled.Security, contentDescription = null, tint = DeepBlue)
						}
					},
					onClick = onInitiateTransaction,
					borderColor = BorderLight
				)
				Spacer(modifier = Modifier.height(16.dp))
				DashboardCard(
					title = "Transaction History",
					subtitle = "View recent operations",
					leading = {
						IconBubble(iconTint = Slate700, background = Cloud200) {
							Icon(imageVector = Icons.Filled.History, contentDescription = null, tint = Slate700)
						}
					},
					onClick = onOpenHistory,
					borderColor = BorderLight
				)
				Spacer(modifier = Modifier.height(16.dp))
				DashboardCard(
					title = "Security Analysis",
					subtitle = "View Quantum vs\nNormal comparison",
					leading = {
						IconBubble(iconTint = Slate700, background = Cloud200) {
							Icon(imageVector = Icons.Filled.ShowChart, contentDescription = null, tint = Slate700)
						}
					},
					onClick = onOpenAnalytics,
					borderColor = BorderLight
				)
				
				Spacer(modifier = Modifier.height(16.dp))
			}
            
			Footer()
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
	val interactionSource = remember { MutableInteractionSource() }
	val isPressed by interactionSource.collectIsPressedAsState()
	val scale by animateFloatAsState(targetValue = if (isPressed) 0.97f else 1f, label = "cardScale")
	Surface(
		shape = RoundedCornerShape(18.dp),
		shadowElevation = 6.dp,
		color = Color.White,
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(18.dp))
			.scale(scale)
			.clickable(
				interactionSource = interactionSource,
				indication = null // Removed ripple/indication parameter which caused ambiguity
			) { onClick() },
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
					Text(title, style = MaterialTheme.typography.titleMedium, color = NightBlack, fontWeight = FontWeight.Medium)
					Spacer(modifier = Modifier.height(4.dp))
					Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Slate800)
				}
			}
			Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = Steel200)
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
					.background(Emerald)
			)
			Spacer(modifier = Modifier.width(8.dp))
			Text("Quantum Network Active", color = Slate800, style = MaterialTheme.typography.labelSmall)
		}
		Spacer(modifier = Modifier.height(6.dp))
		Text(
			"Secured by Quantum Key Distribution",
			color = Steel300Color,
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
