package com.example.quantumaccess.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import com.example.quantumaccess.ui.components.HeaderLogoutButton
import com.example.quantumaccess.ui.components.QuantumButton
import com.example.quantumaccess.ui.components.QuantumLogoBlueOrbits
import com.example.quantumaccess.ui.theme.DeepBlue
import com.example.quantumaccess.ui.theme.QuantumAccessTheme
import com.example.quantumaccess.ui.theme.SecureGreen

@Composable
fun TransactionHistoryScreen(
	modifier: Modifier = Modifier,
	onReturnToDashboard: () -> Unit = {},
	onLoadMore: () -> Unit = {},
	onLogout: () -> Unit = {}
) {
	var selectedFilter by remember { mutableStateOf(HistoryFilter.All) }
	var showLogoutDialog by remember { mutableStateOf(false) }

	val displayedTransactions = remember(selectedFilter) {
		when (selectedFilter) {
			HistoryFilter.All -> sampleTransactions
			HistoryFilter.Quantum -> sampleTransactions.filter { it.type == TransactionType.Quantum }
			HistoryFilter.Normal -> sampleTransactions.filter { it.type == TransactionType.Normal }
		}
	}

	Box(
		modifier = modifier
			.fillMaxSize()
			.background(ScreenBackground)
	) {
		Column(modifier = Modifier.fillMaxSize()) {
			HistoryHeader(onLogout = { showLogoutDialog = true })
			LazyColumn(
				modifier = Modifier
					.fillMaxSize(),
				contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
				verticalArrangement = Arrangement.spacedBy(16.dp)
			) {
				item {
					FilterCard(
						selectedFilter = selectedFilter,
						onFilterSelected = { selectedFilter = it }
					)
				}
				items(displayedTransactions, key = { it.id }) { item ->
					TransactionHistoryCard(item = item)
				}
				item {
					Column(
						modifier = Modifier.fillMaxWidth(),
						verticalArrangement = Arrangement.spacedBy(12.dp)
					) {
						QuantumButton(
							text = "Load More Transactions",
							onClick = onLoadMore
						)
						QuantumButton(
							text = "Return to Dashboard",
							onClick = onReturnToDashboard
						)
					}
				}
			}
		}

		if (showLogoutDialog) {
			HistoryLogoutDialog(
				onCancel = { showLogoutDialog = false },
				onConfirm = {
					showLogoutDialog = false
					onLogout()
				}
			)
		}
	}
}

@Composable
private fun HistoryHeader(onLogout: () -> Unit) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.statusBarsPadding()
			.background(DeepBlue)
			.padding(horizontal = 16.dp, vertical = 14.dp)
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween
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
					Text(
						text = "QuantumAccess",
						color = Color.White,
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.SemiBold
					)
					Text(
						text = "Transaction History",
						color = Color(0xFFCBD5E1),
						style = MaterialTheme.typography.labelSmall
					)
				}
			}
			HeaderLogoutButton(onClick = onLogout)
		}
	}
}

@Composable
private fun FilterCard(
	selectedFilter: HistoryFilter,
	onFilterSelected: (HistoryFilter) -> Unit
) {
	Surface(
		color = Color.White,
		shape = RoundedCornerShape(24.dp),
		shadowElevation = 6.dp,
		modifier = Modifier.fillMaxWidth()
	) {
		Column(modifier = Modifier.padding(20.dp)) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = "Filter Transactions",
					style = MaterialTheme.typography.titleSmall,
					color = Color(0xFF111827),
					fontWeight = FontWeight.Medium
				)
				Icon(
					imageVector = Icons.Outlined.FilterList,
					contentDescription = null,
					tint = DeepBlue
				)
			}
			Spacer(modifier = Modifier.height(16.dp))
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(12.dp)
			) {
				HistoryFilter.values().forEach { filter ->
					HistoryFilterChip(
						label = filter.label,
						selected = filter == selectedFilter,
						onClick = { onFilterSelected(filter) }
					)
				}
			}
		}
	}
}

@Composable
private fun HistoryFilterChip(
	label: String,
	selected: Boolean,
	onClick: () -> Unit
) {
	val background = if (selected) DeepBlue else Color(0xFFF4F6FB)
	val contentColor = if (selected) Color.White else Color(0xFF475569)
	Surface(
		shape = RoundedCornerShape(12.dp),
		color = background,
		border = if (selected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
		modifier = Modifier
			.clip(RoundedCornerShape(12.dp))
			.clickable { onClick() }
	) {
		Text(
			text = label,
			color = contentColor,
			style = MaterialTheme.typography.labelLarge,
			fontWeight = FontWeight.Medium,
			modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
		)
	}
}

@Composable
private fun TransactionHistoryCard(item: TransactionHistoryItem) {
	Surface(
		color = Color.White,
		shape = RoundedCornerShape(24.dp),
		shadowElevation = 4.dp,
		modifier = Modifier.fillMaxWidth()
	) {
		Column(modifier = Modifier.padding(20.dp)) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Row(verticalAlignment = Alignment.CenterVertically) {
					Box(
						modifier = Modifier
							.size(52.dp)
							.clip(CircleShape)
							.background(item.avatarBackground),
						contentAlignment = Alignment.Center
					) {
						Icon(
							imageVector = item.avatarIcon,
							contentDescription = null,
							tint = item.avatarTint,
							modifier = Modifier.size(26.dp)
						)
					}
					Spacer(modifier = Modifier.width(14.dp))
					Column {
						Text(
							text = item.title,
							color = Color(0xFF111827),
							style = MaterialTheme.typography.titleMedium,
							fontWeight = FontWeight.Medium,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis
						)
						Spacer(modifier = Modifier.height(4.dp))
						Text(
							text = item.dateTime,
							color = Color(0xFF94A3B8),
							style = MaterialTheme.typography.bodySmall
						)
					}
				}
				Column(horizontalAlignment = Alignment.End) {
					Text(
						text = item.amount,
						color = item.amountColor,
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.SemiBold
					)
					Spacer(modifier = Modifier.height(6.dp))
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.spacedBy(6.dp)
					) {
						Surface(
							color = item.badgeBackground,
							shape = RoundedCornerShape(50),
							shadowElevation = 0.dp
						) {
							Text(
								text = item.badgeLabel,
								color = item.badgeTextColor,
								style = MaterialTheme.typography.labelMedium,
								fontWeight = FontWeight.Medium,
								modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
							)
						}
						Box(
							modifier = Modifier
								.size(8.dp)
								.clip(CircleShape)
								.background(item.badgeIndicatorColor)
						)
					}
				}
			}
			Spacer(modifier = Modifier.height(16.dp))
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.height(1.dp)
					.background(Color(0xFFE8EDF3))
			)
			Spacer(modifier = Modifier.height(10.dp))
			Row(verticalAlignment = Alignment.CenterVertically) {
				Icon(
					imageVector = item.statusIcon,
					contentDescription = null,
					tint = item.statusColor,
					modifier = Modifier.size(16.dp)
				)
				Spacer(modifier = Modifier.width(8.dp))
				Text(
					text = item.statusText,
					color = item.statusColor,
					style = MaterialTheme.typography.labelSmall,
					fontWeight = FontWeight.Medium
				)
			}
		}
	}
}

@Composable
private fun HistoryLogoutDialog(
	onCancel: () -> Unit,
	onConfirm: () -> Unit
) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(Color.Black.copy(alpha = 0.5f)),
		contentAlignment = Alignment.Center
	) {
		Surface(
			shape = RoundedCornerShape(20.dp),
			shadowElevation = 10.dp,
			color = Color.White,
			modifier = Modifier.padding(horizontal = 24.dp)
		) {
			Column(modifier = Modifier.padding(20.dp)) {
				Text(
					text = "Log Out",
					style = MaterialTheme.typography.titleMedium,
					color = Color(0xFF111827),
					fontWeight = FontWeight.SemiBold
				)
				Spacer(modifier = Modifier.height(8.dp))
				Text(
					text = "Are you sure you want to log out?",
					style = MaterialTheme.typography.bodySmall,
					color = Color(0xFF6B7280)
				)
				Spacer(modifier = Modifier.height(18.dp))
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(12.dp)
				) {
					Surface(
						shape = RoundedCornerShape(14.dp),
						color = Color(0xFFF3F4F6),
						modifier = Modifier
							.weight(1f)
							.clickable { onCancel() }
					) {
						Box(
							contentAlignment = Alignment.Center,
							modifier = Modifier.padding(vertical = 12.dp)
						) {
							Text(
								text = "Cancel",
								color = Color(0xFF374151),
								style = MaterialTheme.typography.labelLarge,
								fontWeight = FontWeight.Medium
							)
						}
					}
					Surface(
						shape = RoundedCornerShape(14.dp),
						color = DeepBlue,
						modifier = Modifier
							.weight(1f)
							.clickable { onConfirm() }
					) {
						Box(
							contentAlignment = Alignment.Center,
							modifier = Modifier.padding(vertical = 12.dp)
						) {
							Text(
								text = "Confirm",
								color = Color.White,
								style = MaterialTheme.typography.labelLarge,
								fontWeight = FontWeight.Medium
							)
						}
					}
				}
			}
		}
	}
}

private enum class HistoryFilter(val label: String) {
	All("All"),
	Quantum("Quantum"),
	Normal("Normal")
}

private enum class TransactionType {
	Quantum,
	Normal
}

private data class TransactionHistoryItem(
	val id: Int,
	val title: String,
	val dateTime: String,
	val amount: String,
	val amountColor: Color,
	val type: TransactionType,
	val badgeLabel: String,
	val badgeBackground: Color,
	val badgeTextColor: Color,
	val badgeIndicatorColor: Color,
	val statusText: String,
	val statusColor: Color,
	val statusIcon: ImageVector,
	val avatarIcon: ImageVector,
	val avatarBackground: Color,
	val avatarTint: Color
)

private val ScreenBackground = Color(0xFFF5F6FB)

private val sampleTransactions = listOf(
	TransactionHistoryItem(
		id = 1,
		title = "Payment to John Smith",
		dateTime = "Dec 15, 2024 14:32",
		amount = "-$1,250.00",
		amountColor = Color(0xFF111827),
		type = TransactionType.Quantum,
		badgeLabel = "Quantum",
		badgeBackground = Color(0xFFE5E9FF),
		badgeTextColor = DeepBlue,
		badgeIndicatorColor = SecureGreen,
		statusText = "Quantum-encrypted - Secure",
		statusColor = SecureGreen,
		statusIcon = Icons.Outlined.Lock,
		avatarIcon = Icons.Rounded.Shield,
		avatarBackground = SecureGreen.copy(alpha = 0.16f),
		avatarTint = SecureGreen
	),
	TransactionHistoryItem(
		id = 2,
		title = "Salary Deposit",
		dateTime = "Dec 14, 2024 09:15",
		amount = "+$5,500.00",
		amountColor = Color(0xFF059669),
		type = TransactionType.Normal,
		badgeLabel = "Normal",
		badgeBackground = Color(0xFFF3F4F6),
		badgeTextColor = Color(0xFF475569),
		badgeIndicatorColor = Color(0xFFCBD5E1),
		statusText = "Standard encryption - Normal",
		statusColor = Color(0xFF6B7280),
		statusIcon = Icons.Outlined.Info,
		avatarIcon = Icons.Rounded.Add,
		avatarBackground = Color(0xFFE2E8FF),
		avatarTint = DeepBlue
	),
	TransactionHistoryItem(
		id = 3,
		title = "ATM Withdrawal",
		dateTime = "Dec 13, 2024 16:45",
		amount = "-$200.00",
		amountColor = Color(0xFF111827),
		type = TransactionType.Quantum,
		badgeLabel = "Quantum",
		badgeBackground = Color(0xFFFDECEC),
		badgeTextColor = Color(0xFFB91C1C),
		badgeIndicatorColor = Color(0xFFDC2626),
		statusText = "Quantum breach detected - Intercepted",
		statusColor = Color(0xFFB91C1C),
		statusIcon = Icons.Outlined.ReportProblem,
		avatarIcon = Icons.Rounded.Warning,
		avatarBackground = Color(0xFFFFE4E6),
		avatarTint = Color(0xFFB91C1C)
	),
	TransactionHistoryItem(
		id = 4,
		title = "Mortgage Payment",
		dateTime = "Dec 10, 2024 12:00",
		amount = "-$2,100.00",
		amountColor = Color(0xFF111827),
		type = TransactionType.Quantum,
		badgeLabel = "Quantum",
		badgeBackground = Color(0xFFE5E9FF),
		badgeTextColor = DeepBlue,
		badgeIndicatorColor = SecureGreen,
		statusText = "Quantum-encrypted - Secure",
		statusColor = SecureGreen,
		statusIcon = Icons.Outlined.Lock,
		avatarIcon = Icons.Rounded.AccountBalance,
		avatarBackground = SecureGreen.copy(alpha = 0.15f),
		avatarTint = SecureGreen
	)
)

@Preview(showBackground = true)
@Composable
private fun TransactionHistoryPreview() {
	QuantumAccessTheme {
		TransactionHistoryScreen()
	}
}

