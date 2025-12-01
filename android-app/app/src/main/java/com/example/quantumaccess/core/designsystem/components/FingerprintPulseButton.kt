package com.example.quantumaccess.core.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.quantumaccess.core.designsystem.theme.DeepBlue

@Composable
fun FingerprintPulseButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val glowAlpha by transition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .size(120.dp)
            .graphicsLayer { shadowElevation = 0f }
            .background(
                color = Color.Transparent,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .scale(scale)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            DeepBlue.copy(alpha = glowAlpha),
                            Color(0xFF0D47A1).copy(alpha = 0f)
                        )
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(scale)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(DeepBlue, Color(0xFF0D47A1))
                    ),
                    shape = CircleShape
                )
                .clickable(
                    onClick = onClick,
                    interactionSource = androidx.compose.foundation.interaction.MutableInteractionSource(),
                    indication = androidx.compose.material3.ripple(bounded = true, radius = 50.dp, color = Color.White)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Fingerprint,
                contentDescription = "Fingerprint",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

