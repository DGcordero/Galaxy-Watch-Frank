package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ComplicationSlot
import com.example.model.ComplicationType
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplicationPickerSheet(
    slot: ComplicationSlot,
    currentType: ComplicationType,
    onSelectType: (ComplicationType) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = TitaniumGray)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Configurar Ranura: ${slot.displayName}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Selecciona una métrica dinámica para tu Galaxy Watch",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Grouped complications
            val complications = ComplicationType.values().toList()
            val categories = complications.groupBy { it.category }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                categories.forEach { (category, items) ->
                    item {
                        Text(
                            text = category.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GalaxyCyan,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }

                    items(items) { type ->
                        val isSelected = type == currentType
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onSelectType(type)
                                    onDismiss()
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) GalaxyCyan.copy(alpha = 0.15f) else DarkSurfaceVariant
                            ),
                            border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(GalaxyCyan),
                                width = 1.5.dp
                            ) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) GalaxyCyan else DarkBorder
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getComplicationIcon(type),
                                            contentDescription = null,
                                            tint = if (isSelected) DarkBackground else TextPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = type.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                        if (type.defaultUnit.isNotEmpty()) {
                                            Text(
                                                text = "Unidad: ${type.defaultUnit}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextTertiary
                                            )
                                        }
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Seleccionado",
                                        tint = GalaxyCyan
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun getComplicationIcon(type: ComplicationType): ImageVector {
    return when (type) {
        ComplicationType.HEART_RATE -> Icons.Default.Favorite
        ComplicationType.STEPS -> Icons.Default.DirectionsWalk
        ComplicationType.CALORIES -> Icons.Default.LocalFireDepartment
        ComplicationType.BATTERY_WATCH -> Icons.Default.Watch
        ComplicationType.BATTERY_PHONE -> Icons.Default.Smartphone
        ComplicationType.WEATHER_TEMP -> Icons.Default.WbSunny
        ComplicationType.UV_INDEX -> Icons.Default.WbIridescent
        ComplicationType.SLEEP_SCORE -> Icons.Default.Bedtime
        ComplicationType.STRESS_LEVEL -> Icons.Default.Psychology
        ComplicationType.DISTANCE -> Icons.Default.Straighten
        ComplicationType.SUNRISE_SUNSET -> Icons.Default.WbTwilight
        ComplicationType.NEXT_EVENT -> Icons.Default.Event
        ComplicationType.WORLD_CLOCK -> Icons.Default.Public
        ComplicationType.MOON_PHASE -> Icons.Default.NightlightRound
        ComplicationType.BAROMETER -> Icons.Default.Speed
        ComplicationType.DATE_BADGE -> Icons.Default.CalendarToday
        ComplicationType.NONE -> Icons.Default.Block
    }
}
