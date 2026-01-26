package com.example.quantumaccess.feature.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quantumaccess.core.designsystem.theme.BorderLight
import com.example.quantumaccess.core.designsystem.theme.DeepBlue
import com.example.quantumaccess.core.designsystem.theme.NightBlack
import com.example.quantumaccess.core.designsystem.theme.SecureGreen
import com.example.quantumaccess.core.designsystem.theme.Slate800
import com.example.quantumaccess.core.designsystem.theme.Steel300
import com.example.quantumaccess.domain.model.SecurityScoreSummary

/**
 * Card that displays security scores in the Dashboard.
 * Shows Normal and Quantum scores with informative text.
 */
@Composable
fun SecurityScoreCard(
    scoreSummary: SecurityScoreSummary,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(DeepBlue, DeepBlue.copy(alpha = 0.7f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Security Impact Score",
                        style = MaterialTheme.typography.titleMedium,
                        color = NightBlack,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Last updated: ${scoreSummary.lastUpdated}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Steel300
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Scores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ScoreCircle(
                    label = "Normal Security",
                    score = scoreSummary.normalScore,
                    color = Steel300,
                    isHighlighted = false
                )
                ScoreCircle(
                    label = "Quantum Security (QKD)",
                    score = scoreSummary.quantumScore,
                    color = SecureGreen,
                    isHighlighted = true
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Info text
            Text(
                text = "Quantum algorithms provide protection against future quantum computer attacks.",
                style = MaterialTheme.typography.bodySmall,
                color = Slate800,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun ScoreCircle(
    label: String,
    score: Int,
    color: Color,
    isHighlighted: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    if (isHighlighted)
                        Brush.linearGradient(listOf(color.copy(alpha = 0.15f), color.copy(alpha = 0.05f)))
                    else
                        Brush.linearGradient(listOf(Color(0xFFF5F5F5), Color(0xFFEEEEEE)))
                )
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (isHighlighted) color else NightBlack,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "/100",
                    style = MaterialTheme.typography.labelSmall,
                    color = Steel300
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isHighlighted) color else Slate800,
            fontWeight = if (isHighlighted) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SecurityScoreCardPreview() {
    SecurityScoreCard(
        scoreSummary = SecurityScoreSummary(
            normalScore = 72,
            quantumScore = 95,
            transactionCount = 15,
            lastUpdated = "Jan 22, 14:30"
        )
    )
}
