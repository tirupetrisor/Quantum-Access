package com.example.quantumaccess.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.quantumaccess.core.designsystem.theme.DeepBlue

/**
 * Circular logout control that shows a confirmation dialog before invoking [onConfirmLogout].
 * Use this component anywhere you need logout behaviour without re-implementing dialog logic.
 */
@Composable
fun LogoutButton(
    onConfirmLogout: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White.copy(alpha = 0.18f),
    iconTint: Color = Color.White,
    size: Dp = 40.dp,
    iconSize: Dp = 18.dp,
    dialogTitle: String = "Log Out",
    dialogMessage: String = "Are you sure you want to log out?",
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    confirmButtonColor: Color = DeepBlue,
    cancelButtonColor: Color = Color(0xFFF3F4F6),
    confirmTextColor: Color = Color.White,
    cancelTextColor: Color = Color(0xFF374151),
    overlayColor: Color = Color.Black.copy(alpha = 0.55f)
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable { showDialog = true },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.PowerSettingsNew,
            contentDescription = "Logout",
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }

    if (showDialog) {
        LogoutConfirmationDialog(
            title = dialogTitle,
            message = dialogMessage,
            confirmText = confirmText,
            cancelText = cancelText,
            confirmButtonColor = confirmButtonColor,
            cancelButtonColor = cancelButtonColor,
            confirmTextColor = confirmTextColor,
            cancelTextColor = cancelTextColor,
            overlayColor = overlayColor,
            onDismiss = { showDialog = false },
            onConfirm = {
                showDialog = false
                onConfirmLogout()
            }
        )
    }
}

@Composable
private fun LogoutConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    cancelText: String,
    confirmButtonColor: Color,
    cancelButtonColor: Color,
    confirmTextColor: Color,
    cancelTextColor: Color,
    overlayColor: Color,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(overlayColor),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                shadowElevation = 12.dp,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280)
                    )
                    Spacer(modifier = Modifier.size(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = cancelButtonColor,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onDismiss() }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cancelText,
                                    color = cancelTextColor,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = confirmButtonColor,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onConfirm() }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = confirmText,
                                    color = confirmTextColor,
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
}

