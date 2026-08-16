package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.model.ComplicationSlot
import com.example.model.ComplicationType
import com.example.model.GalaxyHealthSnapshot
import com.example.ui.theme.*

@Composable
fun ComplicationDetailDialog(
    slot: ComplicationSlot,
    type: ComplicationType,
    healthData: GalaxyHealthSnapshot,
    onAdjustMetric: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DarkSurface,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(DarkBorder),
                width = 1.dp
            ),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon + Slot indicator
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(GalaxyCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getComplicationIcon(type),
                        contentDescription = null,
                        tint = GalaxyCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = type.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "Ranura: ${slot.displayName} • Galaxy Health Sync",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Detail display card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        when (type) {
                            ComplicationType.HEART_RATE -> {
                                HealthRow("Pulso Actual", "${healthData.heartRateBpm} BPM", GalaxyNeonPink)
                                HealthRow("Estado", healthData.heartRateStatus, TextSecondary)
                                HealthRow("Zona de Entrenamiento", "Zona 2 (Quema de grasa)", GalaxyEmerald)
                            }
                            ComplicationType.STEPS -> {
                                HealthRow("Pasos de Hoy", "${healthData.dailySteps} / ${healthData.stepGoal}", GalaxyEmerald)
                                HealthRow("Meta Cumplida", "${(healthData.stepProgress * 100).toInt()}%", GalaxyCyan)
                                HealthRow("Distancia Estimada", "${healthData.distanceKm} km", TextSecondary)
                            }
                            ComplicationType.CALORIES -> {
                                HealthRow("Calorías Activas", "${healthData.activeCalories} kcal", GalaxyUltraOrange)
                                HealthRow("Meta Diaria", "${healthData.calorieGoal} kcal", TextSecondary)
                            }
                            ComplicationType.BATTERY_WATCH -> {
                                HealthRow("Batería Galaxy Watch", "${healthData.watchBatteryLevel}%", GalaxyCyan)
                                HealthRow("Autonomía Estimada", "36 horas restantes", TextSecondary)
                                HealthRow("Consumo AOD", "~0.8%/hora", GalaxyEmerald)
                            }
                            ComplicationType.BATTERY_PHONE -> {
                                HealthRow("Batería S25 Ultra", "${healthData.phoneBatteryLevel}%", GalaxyBlue)
                                HealthRow("Carga Rápida 45W", "Listo", TextSecondary)
                            }
                            ComplicationType.WEATHER_TEMP -> {
                                HealthRow("Temperatura", "${healthData.temperatureCelsius}°C", GalaxyAmber)
                                HealthRow("Condición", healthData.weatherCondition, TextSecondary)
                                HealthRow("Índice UV", "Nivel ${healthData.uvIndex} (Moderado)", GalaxyPurple)
                            }
                            ComplicationType.SLEEP_SCORE -> {
                                HealthRow("Puntuación de Sueño", "${healthData.sleepScore} / 100", GalaxyCyan)
                                HealthRow("Duración", "${healthData.sleepDurationHours} horas", TextSecondary)
                                HealthRow("Fase REM", "1h 45m (Óptimo)", GalaxyEmerald)
                            }
                            ComplicationType.STRESS_LEVEL -> {
                                HealthRow("Nivel de Estrés", "${healthData.stressLevel} / 100", GalaxyUltraOrange)
                                HealthRow("Recomendación", "Respiración Guiada 2 min", GalaxyCyan)
                            }
                            else -> {
                                HealthRow("Métrica", type.title, TextPrimary)
                                HealthRow("Estado de Sincronización", "Activo y Calibrado", GalaxyEmerald)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive value simulator slider / adjustments
                Text(
                    text = "Simular Valor para Pruebas en Vivo",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = { onAdjustMetric(-5) },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = DarkBorder)
                    ) {
                        Text("-5", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = { onAdjustMetric(-1) },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = DarkBorder)
                    ) {
                        Text("-1", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = { onAdjustMetric(1) },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = GalaxyCyan.copy(alpha = 0.2f))
                    ) {
                        Text("+1", color = GalaxyCyan, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = { onAdjustMetric(5) },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = GalaxyCyan.copy(alpha = 0.2f))
                    ) {
                        Text("+5", color = GalaxyCyan, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GalaxyCyan)
                ) {
                    Text("Cerrar", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun HealthRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = valueColor)
    }
}
