package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ComplicationType
import com.example.ui.theme.*
import com.example.ui.viewmodel.WatchStudioViewModel

@Composable
fun HealthMetricsScreen(
    viewModel: WatchStudioViewModel,
    modifier: Modifier = Modifier
) {
    val healthData by viewModel.healthSnapshot.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Métricas de Salud Galaxy",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Sensor BioActive 3-en-1 calibrado",
                    style = MaterialTheme.typography.bodySmall,
                    color = GalaxyEmerald
                )
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(GalaxyEmerald.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = GalaxyEmerald)
            }
        }

        // Quick Daily Rings Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Metas de Actividad Diaria",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                HealthProgressRow(
                    icon = Icons.Default.DirectionsWalk,
                    title = "Pasos",
                    currentValue = "${healthData.dailySteps}",
                    goalValue = "${healthData.stepGoal}",
                    unit = "pasos",
                    progress = healthData.stepProgress,
                    color = GalaxyEmerald,
                    onIncrease = { viewModel.updateHealthMetric(ComplicationType.STEPS, 4) },
                    onDecrease = { viewModel.updateHealthMetric(ComplicationType.STEPS, -4) }
                )

                HealthProgressRow(
                    icon = Icons.Default.LocalFireDepartment,
                    title = "Calorías Activas",
                    currentValue = "${healthData.activeCalories}",
                    goalValue = "${healthData.calorieGoal}",
                    unit = "kcal",
                    progress = healthData.calorieProgress,
                    color = GalaxyUltraOrange,
                    onIncrease = { viewModel.updateHealthMetric(ComplicationType.CALORIES, 2) },
                    onDecrease = { viewModel.updateHealthMetric(ComplicationType.CALORIES, -2) }
                )

                HealthProgressRow(
                    icon = Icons.Default.Favorite,
                    title = "Frecuencia Cardíaca",
                    currentValue = "${healthData.heartRateBpm}",
                    goalValue = "Reposo",
                    unit = "BPM",
                    progress = (healthData.heartRateBpm - 40f) / 140f,
                    color = GalaxyNeonPink,
                    onIncrease = { viewModel.updateHealthMetric(ComplicationType.HEART_RATE, 3) },
                    onDecrease = { viewModel.updateHealthMetric(ComplicationType.HEART_RATE, -3) }
                )
            }
        }

        // Detailed Health Metric Cards Grid
        Text(
            text = "Parámetros de Sensores para Esferas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Sleep Score Card
            MetricInteractiveCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Bedtime,
                title = "Sueño",
                value = "${healthData.sleepScore} pts",
                subtext = "${healthData.sleepDurationHours}h • Óptimo",
                color = GalaxyCyan,
                onInc = { viewModel.updateHealthMetric(ComplicationType.SLEEP_SCORE, 2) },
                onDec = { viewModel.updateHealthMetric(ComplicationType.SLEEP_SCORE, -2) }
            )

            // Stress Level Card
            MetricInteractiveCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Psychology,
                title = "Estrés",
                value = "${healthData.stressLevel} / 100",
                subtext = "Nivel Bajo",
                color = GalaxyAmber,
                onInc = { viewModel.updateHealthMetric(ComplicationType.STRESS_LEVEL, 4) },
                onDec = { viewModel.updateHealthMetric(ComplicationType.STRESS_LEVEL, -4) }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Weather & Temp
            MetricInteractiveCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.WbSunny,
                title = "Temperatura",
                value = "${healthData.temperatureCelsius}°C",
                subtext = healthData.weatherCondition,
                color = GalaxyAmber,
                onInc = { viewModel.updateHealthMetric(ComplicationType.WEATHER_TEMP, 1) },
                onDec = { viewModel.updateHealthMetric(ComplicationType.WEATHER_TEMP, -1) }
            )

            // Watch Battery
            MetricInteractiveCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Watch,
                title = "Batería Reloj",
                value = "${healthData.watchBatteryLevel}%",
                subtext = "Galaxy Watch 7",
                color = GalaxyEmerald,
                onInc = { viewModel.updateHealthMetric(ComplicationType.BATTERY_WATCH, 5) },
                onDec = { viewModel.updateHealthMetric(ComplicationType.BATTERY_WATCH, -5) }
            )
        }
    }
}

@Composable
private fun HealthProgressRow(
    icon: ImageVector,
    title: String,
    currentValue: String,
    goalValue: String,
    unit: String,
    progress: Float,
    color: Color,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                Text(title, fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 13.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "$currentValue / $goalValue $unit",
                    fontWeight = FontWeight.Bold,
                    color = color,
                    fontSize = 13.sp
                )
                // Adjustment buttons
                IconButton(onClick = onDecrease, modifier = Modifier.size(26.dp)) {
                    Text("-", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                IconButton(onClick = onIncrease, modifier = Modifier.size(26.dp)) {
                    Text("+", color = GalaxyCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = DarkBorder
        )
    }
}

@Composable
private fun MetricInteractiveCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    subtext: String,
    color: Color,
    onInc: () -> Unit,
    onDec: () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                Row {
                    IconButton(onClick = onDec, modifier = Modifier.size(24.dp)) {
                        Text("-", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    IconButton(onClick = onInc, modifier = Modifier.size(24.dp)) {
                        Text("+", color = GalaxyCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(subtext, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
    }
}
