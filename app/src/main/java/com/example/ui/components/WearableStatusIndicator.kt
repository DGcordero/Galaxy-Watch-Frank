package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionStatus
import com.example.ui.theme.*

/**
 * WearableStatusIndicator
 *
 * Displays a clean, modern status indicator for the Wearable Data Layer connection state:
 * - 'Connected' (Pulsing Emerald glow & active badge)
 * - 'Disconnected' (Coral/Red badge indicating offline/reconnecting)
 * - 'Syncing' (Amber active sync indicator)
 */
@Composable
fun WearableStatusIndicator(
    status: ConnectionStatus,
    modifier: Modifier = Modifier,
    deviceName: String? = null,
    onClick: (() -> Unit)? = null,
    showIcon: Boolean = true,
    compact: Boolean = false
) {
    val isConnected = status == ConnectionStatus.CONNECTED
    val isSyncing = status == ConnectionStatus.SYNCING

    val primaryColor by animateColorAsState(
        targetValue = when (status) {
            ConnectionStatus.CONNECTED -> GalaxyEmerald
            ConnectionStatus.SYNCING -> GalaxyAmber
            ConnectionStatus.DISCONNECTED -> Color(0xFFFF5252)
        },
        animationSpec = tween(300),
        label = "status_color"
    )

    val displayText = when (status) {
        ConnectionStatus.CONNECTED -> "Connected"
        ConnectionStatus.SYNCING -> "Syncing..."
        ConnectionStatus.DISCONNECTED -> "Disconnected"
    }

    // Infinite breathing pulse for connected state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_trans")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isConnected || isSyncing) 1.35f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = if (isConnected || isSyncing) 0.15f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .testTag("wearable_status_indicator")
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple(bounded = true, color = primaryColor),
                        onClick = onClick
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        color = primaryColor.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = primaryColor.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = if (compact) 8.dp else 10.dp,
                    vertical = if (compact) 4.dp else 6.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Pulsing dot indicator
            Box(
                modifier = Modifier.size(10.dp),
                contentAlignment = Alignment.Center
            ) {
                // Pulse halo
                if (isConnected || isSyncing) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = pulseAlpha))
                    )
                }
                // Solid center dot
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(primaryColor)
                        .testTag("wearable_status_dot")
                )
            }

            if (showIcon) {
                Icon(
                    imageVector = when (status) {
                        ConnectionStatus.CONNECTED -> Icons.Default.Watch
                        ConnectionStatus.SYNCING -> Icons.Default.Sync
                        ConnectionStatus.DISCONNECTED -> Icons.Default.WatchOff
                    },
                    contentDescription = null,
                    modifier = Modifier.size(13.dp),
                    tint = primaryColor
                )
            }

            // Status label: "Connected" / "Disconnected"
            Text(
                text = displayText,
                style = MaterialTheme.typography.labelSmall,
                fontSize = if (compact) 10.5.sp else 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                modifier = Modifier.testTag("wearable_status_text")
            )

            if (!compact && !deviceName.isNullOrBlank() && isConnected) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = primaryColor.copy(alpha = 0.6f)
                )
                Text(
                    text = deviceName,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextSecondary,
                    maxLines = 1
                )
            }
        }
    }
}
