package com.example.quantumaccess.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.quantumaccess.core.designsystem.theme.DeepBlue
import com.example.quantumaccess.core.designsystem.theme.MistBlue

/**
 * Shared top header used across screens that follow the QuantumAccess branding.
 */
@Composable
fun QuantumTopBar(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    backgroundColor: Color = DeepBlue,
    titleColor: Color = Color.White,
    subtitleColor: Color = MistBlue,
    leadingContent: (@Composable () -> Unit)? = { DefaultHeaderLogo() },
    trailingContent: (@Composable () -> Unit)? = null,
    showLogoutButton: Boolean = false,
    onLogoutClick: () -> Unit = {}
) {
    val trailingSlot: (@Composable () -> Unit)? = trailingContent ?: if (showLogoutButton) {
        { LogoutButton(onConfirmLogout = onLogoutClick) }
    } else {
        null
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                leadingContent?.let {
                    it()
                    Spacer(modifier = Modifier.size(10.dp))
                }
                Column {
                    Text(
                        text = title,
                        color = titleColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.size(2.dp))
                        Text(
                            text = subtitle,
                            color = subtitleColor,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
            trailingSlot?.invoke()
        }
    }
}

@Composable
fun DefaultHeaderLogo(
    size: Dp = 36.dp,
    backgroundColor: Color = Color.White,
    cornerRadius: Dp = 10.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        QuantumLogoBlueOrbits(showText = false, iconSize = size * 0.66f)
    }
}

