package com.example.quantumaccess.feature.history.presentation

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
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.quantumaccess.core.designsystem.components.PrimaryActionButton
import com.example.quantumaccess.core.designsystem.components.QuantumTopBar
import com.example.quantumaccess.core.designsystem.theme.AlertIndicatorRed
import com.example.quantumaccess.core.designsystem.theme.AlertRed
import com.example.quantumaccess.core.designsystem.theme.AlertRedDark
import com.example.quantumaccess.core.designsystem.theme.BorderLight
import com.example.quantumaccess.core.designsystem.theme.BorderMuted
import com.example.quantumaccess.core.designsystem.theme.Cloud100
import com.example.quantumaccess.core.designsystem.theme.Cloud200
import com.example.quantumaccess.core.designsystem.theme.Cloud250
import com.example.quantumaccess.core.designsystem.theme.DeepBlue
import com.example.quantumaccess.core.designsystem.theme.MintBadge
import com.example.quantumaccess.core.designsystem.theme.MistBlue
import com.example.quantumaccess.core.designsystem.theme.NightBlack
import com.example.quantumaccess.core.designsystem.theme.QuantumAccessTheme
import com.example.quantumaccess.core.designsystem.theme.RoseBadge
import com.example.quantumaccess.core.designsystem.theme.SecureGreen
import com.example.quantumaccess.core.designsystem.theme.Slate600
import com.example.quantumaccess.core.designsystem.theme.Slate800
import com.example.quantumaccess.core.designsystem.theme.Steel200
import com.example.quantumaccess.core.designsystem.theme.Steel300
import com.example.quantumaccess.data.sample.RepositoryProvider
import com.example.quantumaccess.domain.model.TransactionChannel
import com.example.quantumaccess.domain.model.TransactionDirection
import com.example.quantumaccess.domain.model.TransactionHistoryEntry
import com.example.quantumaccess.domain.model.TransactionSecurityState
import com.example.quantumaccess.domain.repository.TransactionRepository
import kotlinx.coroutines.launch

@Composable
fun TransactionHistoryScreen(
	modifier: Modifier = Modifier,
	onReturnToDashboard: () -> Unit = {},
	onLoadMore: () -> Unit = {},
	onLogout: () -> Unit = {},
	transactionRepository: TransactionRepository = RepositoryProvider.transactionRepository
) {
	val coroutineScope = rememberCoroutineScope()
	val transactions by transactionRepository
		.observeTransactionHistory()
		.collectAsState(initial = emptyList())
	var selectedFilter by remember { mutableStateOf(HistoryFilter.All) }
	var visibleCount by remember { mutableIntStateOf(3) }

	val filteredTransactions = remember(selectedFilter, transactions) {
		when (selectedFilter) {
			HistoryFilter.All -> transactions
			HistoryFilter.Quantum -> transactions.filter { it.channel == TransactionChannel.QUANTUM }
			HistoryFilter.Normal -> transactions.filter { it.channel == TransactionChannel.NORMAL }
		}
	}

	val displayedTransactions = remember(filteredTransactions, visibleCount) {
		filteredTransactions.take(visibleCount)
	}

	// Reset visible count when filter changes
	LaunchedEffect(selectedFilter) {
		visibleCount = 3
	}

	fun duplicateTransaction(entry: TransactionHistoryEntry) {
		coroutineScope.launch {
			val mode = when (entry.channel) {
				TransactionChannel.QUANTUM -> "QUANTUM"
				TransactionChannel.NORMAL -> "NORMAL"
			}
			transactionRepository.insertTransaction(
				amount = entry.amountValue,
				mode = mode,
				status = DuplicateStatusMessage,
				intercepted = false,
				beneficiary = entry.beneficiary
			)
		}
	}

	Box(
		modifier = modifier
			.fillMaxSize()
			.background(ScreenBackground)
	) {
		Column(modifier = Modifier.fillMaxSize()) {
			QuantumTopBar(
				title = "QuantumAccess",
				subtitle = "Transaction History",
				showLogoutButton = true,
				onLogoutClick = onLogout
			)
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
				items(displayedTransactions, key = { it.id }) { entry ->
					TransactionHistoryCard(
						entry = entry,
						onDuplicate = { duplicateTransaction(entry) }
					)
				}
				item {
					Column(
						modifier = Modifier.fillMaxWidth(),
						verticalArrangement = Arrangement.spacedBy(12.dp)
					) {
						if (visibleCount < filteredTransactions.size) {
							PrimaryActionButton(
								text = "Load More Transactions",
								onClick = { visibleCount += 3 }
							)
						}
						PrimaryActionButton(
							text = "Return to Dashboard",
							onClick = onReturnToDashboard
						)
					}
				}
			}
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
					color = NightBlack,
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
	val background = if (selected) DeepBlue else Cloud200
	val contentColor = if (selected) Color.White else Slate600
	Surface(
		shape = RoundedCornerShape(12.dp),
		color = background,
		border = if (selected) null else BorderStroke(1.dp, BorderMuted),
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
private fun TransactionHistoryCard(
	entry: TransactionHistoryEntry,
	onDuplicate: () -> Unit
) {
	val uiModel = remember(entry) { entry.toUiModel() }
	Surface(
		color = Color.White,
		shape = RoundedCornerShape(16.dp),
		shadowElevation = 3.dp,
		modifier = Modifier.fillMaxWidth()
	) {
		Column(modifier = Modifier.padding(14.dp)) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Row(verticalAlignment = Alignment.CenterVertically) {
					Box(
						modifier = Modifier
							.size(42.dp)
							.clip(CircleShape)
							.background(uiModel.avatarBackground),
						contentAlignment = Alignment.Center
					) {
						Icon(
							imageVector = uiModel.avatarIcon,
							contentDescription = null,
							tint = uiModel.avatarTint,
							modifier = Modifier.size(22.dp)
						)
					}
					Spacer(modifier = Modifier.width(12.dp))
					Column {
						Text(
							text = entry.title,
							color = NightBlack,
							style = MaterialTheme.typography.titleSmall,
							fontWeight = FontWeight.Medium,
							maxLines = 1,
							overflow = TextOverflow.Ellipsis
						)
						Spacer(modifier = Modifier.height(2.dp))
						Text(
							text = entry.dateTime,
							color = Steel300,
							style = MaterialTheme.typography.labelSmall
						)
					}
				}
				Column(horizontalAlignment = Alignment.End) {
					Text(
						text = uiModel.amount,
						color = uiModel.amountColor,
						style = MaterialTheme.typography.titleSmall,
						fontWeight = FontWeight.SemiBold
					)
					Spacer(modifier = Modifier.height(4.dp))
					Row(
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.spacedBy(5.dp)
					) {
						Surface(
							color = uiModel.badgeBackground,
							shape = RoundedCornerShape(50),
							shadowElevation = 0.dp
						) {
							Text(
								text = uiModel.badgeLabel,
								color = uiModel.badgeTextColor,
								style = MaterialTheme.typography.labelSmall,
								fontWeight = FontWeight.Medium,
								modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
							)
						}
						Box(
							modifier = Modifier
								.size(6.dp)
								.clip(CircleShape)
								.background(uiModel.badgeIndicatorColor)
						)
					}
				}
			}
			Spacer(modifier = Modifier.height(12.dp))
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.height(1.dp)
					.background(Cloud250)
			)
			Spacer(modifier = Modifier.height(8.dp))
			Row(verticalAlignment = Alignment.CenterVertically) {
				Icon(
					imageVector = uiModel.statusIcon,
					contentDescription = null,
					tint = uiModel.statusColor,
					modifier = Modifier.size(14.dp)
				)
				Spacer(modifier = Modifier.width(6.dp))
				Text(
					text = entry.statusMessage,
					color = uiModel.statusColor,
					style = MaterialTheme.typography.labelSmall,
					fontWeight = FontWeight.Medium
				)
			}
			Spacer(modifier = Modifier.height(12.dp))
			TransactionRecipientRow(
				recipient = uiModel.recipientName,
				onDuplicate = onDuplicate
			)
		}
	}
}

@Composable
private fun TransactionRecipientRow(
	recipient: String,
	onDuplicate: () -> Unit
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Column(
			modifier = Modifier.weight(1f),
			verticalArrangement = Arrangement.spacedBy(2.dp)
		) {
			Text(
				text = "Sent to",
				color = Slate600,
				style = MaterialTheme.typography.labelSmall,
				fontWeight = FontWeight.Medium
			)
			Text(
				text = recipient,
				color = NightBlack,
				style = MaterialTheme.typography.bodyMedium,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}
		TextButton(onClick = onDuplicate) {
			Icon(
				imageVector = Icons.Rounded.ContentCopy,
				contentDescription = null,
				tint = DeepBlue,
				modifier = Modifier.size(16.dp)
			)
			Spacer(modifier = Modifier.width(6.dp))
			Text(
				text = "Duplicate",
				color = DeepBlue,
				style = MaterialTheme.typography.labelMedium,
				fontWeight = FontWeight.SemiBold
			)
		}
	}
}

private enum class HistoryFilter(val label: String) {
	All("All"),
	Quantum("Quantum"),
	Normal("Normal")
}

private val ScreenBackground = Cloud100
private const val DuplicateStatusMessage = "Duplicated from history"

private data class TransactionHistoryUiModel(
	val amount: String,
	val amountColor: Color,
	val badgeLabel: String,
	val badgeBackground: Color,
	val badgeTextColor: Color,
	val badgeIndicatorColor: Color,
	val statusColor: Color,
	val statusIcon: ImageVector,
	val avatarIcon: ImageVector,
	val avatarBackground: Color,
	val avatarTint: Color,
	val recipientName: String
)

private fun TransactionHistoryEntry.toUiModel(): TransactionHistoryUiModel {
	val amountColor = when (direction) {
		TransactionDirection.CREDIT -> Color(0xFF059669)
		TransactionDirection.DEBIT -> NightBlack
	}
	val badgeLabel = when (channel) {
		TransactionChannel.QUANTUM -> "Quantum"
		TransactionChannel.NORMAL -> "Normal"
	}
	val badgeBackground = when (channel) {
		TransactionChannel.QUANTUM -> MintBadge
		TransactionChannel.NORMAL -> Cloud200
	}
	val badgeTextColor = when (channel) {
		TransactionChannel.QUANTUM -> DeepBlue
		TransactionChannel.NORMAL -> Slate600
	}
	val badgeIndicatorColor = when (securityState) {
		TransactionSecurityState.SECURE -> SecureGreen
		TransactionSecurityState.NORMAL -> MistBlue
		TransactionSecurityState.ALERT -> AlertIndicatorRed
	}
	val statusColor = when (securityState) {
		TransactionSecurityState.SECURE -> SecureGreen
		TransactionSecurityState.NORMAL -> Slate800
		TransactionSecurityState.ALERT -> AlertRedDark
	}
	val statusIcon = when (securityState) {
		TransactionSecurityState.SECURE -> Icons.Outlined.Lock
		TransactionSecurityState.NORMAL -> Icons.Outlined.Info
		TransactionSecurityState.ALERT -> Icons.Outlined.ReportProblem
	}
	val avatarIcon = when {
		securityState == TransactionSecurityState.ALERT -> Icons.Rounded.Warning
		channel == TransactionChannel.QUANTUM -> Icons.Rounded.Shield
		else -> Icons.Rounded.AccountBalance
	}
	val avatarBackground = when {
		securityState == TransactionSecurityState.ALERT -> AlertRedDark.copy(alpha = 0.15f)
		channel == TransactionChannel.QUANTUM -> SecureGreen.copy(alpha = 0.16f)
		else -> Color(0xFFE2E8FF)
	}
	val avatarTint = when {
		securityState == TransactionSecurityState.ALERT -> AlertRedDark
		channel == TransactionChannel.QUANTUM -> SecureGreen
		else -> DeepBlue
	}
	return TransactionHistoryUiModel(
		amount = amountFormatted,
		amountColor = amountColor,
		badgeLabel = badgeLabel,
		badgeBackground = badgeBackground,
		badgeTextColor = badgeTextColor,
		badgeIndicatorColor = badgeIndicatorColor,
		statusColor = statusColor,
		statusIcon = statusIcon,
		avatarIcon = avatarIcon,
		avatarBackground = avatarBackground,
		avatarTint = avatarTint,
		recipientName = beneficiary
	)
}

@Preview(showBackground = true)
@Composable
private fun TransactionHistoryPreview() {
	QuantumAccessTheme {
		TransactionHistoryScreen()
	}
}

