package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*

@Composable
fun ImportWatchFaceDialog(
    onImportJson: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var jsonText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val sampleCommunityDesigns = listOf(
        "Neon Synthwave 84" to """{
  "title": "Neon Synthwave 84",
  "description": "Estilo retro futurista con acentos fucsia y turquesa",
  "author": "RetroSynth",
  "category": "Digital",
  "dialType": "DIGITAL_CYBER",
  "backgroundPattern": "CYBER_GRID",
  "primaryColor": 4294901968,
  "accentColor": 4278255615,
  "dialBackgroundColor": 4280295716,
  "handsColor": 4294901968,
  "secondHandColor": 4278255615,
  "subdialColor": 4281739575,
  "glowColor": 4294901968,
  "handStyle": "NEON_BEAM",
  "secondHandMovement": "SWEEP_60FPS",
  "hourMarkerStyle": "NONE",
  "bezelStyle": "INNER_SECONDS_TRACK",
  "fontFamily": "ORBITRON",
  "complicationTop": "HEART_RATE",
  "complicationBottom": "STEPS",
  "complicationLeft": "BATTERY_WATCH",
  "complicationRight": "WEATHER_TEMP"
}""",
        "Rose Gold Minimalist" to """{
  "title": "Rose Gold Minimalist",
  "description": "Elegancia pura en oro rosa y fondo negro",
  "author": "Aura Luxury",
  "category": "Minimalista",
  "dialType": "ANALOG_MINIMAL",
  "backgroundPattern": "AMOLED_BLACK",
  "primaryColor": 4293708453,
  "accentColor": 4294953920,
  "dialBackgroundColor": 4278190080,
  "handsColor": 4294967295,
  "secondHandColor": 4293708453,
  "subdialColor": 4280427042,
  "glowColor": 4293708453,
  "handStyle": "MINIMAL_BAR",
  "secondHandMovement": "TICK_1HZ",
  "hourMarkerStyle": "MINIMAL_DOTS",
  "bezelStyle": "MINIMAL_RING",
  "fontFamily": "MONTSERRAT",
  "complicationTop": "WEATHER_TEMP",
  "complicationBottom": "BATTERY_WATCH",
  "complicationLeft": "NEXT_EVENT",
  "complicationRight": "STEPS"
}"""
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = DarkSurface,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(DarkBorder),
                width = 1.dp
            ),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Importar Esfera",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondary)
                    }
                }

                Text(
                    text = "Pega un código JSON .gwatchface compartido por otro usuario:",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = jsonText,
                    onValueChange = {
                        jsonText = it
                        errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    placeholder = {
                        Text(
                            "Pega aquí el JSON de la esfera...",
                            color = TextTertiary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    textStyle = LocalTextStyle.current.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = TextPrimary
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF070A10),
                        unfocusedContainerColor = Color(0xFF070A10),
                        focusedBorderColor = GalaxyCyan,
                        unfocusedBorderColor = DarkBorder
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFFF5252),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "O cargar diseño de la comunidad de ejemplo:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = GalaxyCyan
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sampleCommunityDesigns.forEach { (name, sampleJson) ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    jsonText = sampleJson
                                    errorMessage = null
                                },
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                        ) {
                            Text(
                                text = name,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (jsonText.isBlank()) {
                            errorMessage = "Por favor ingresa un código JSON válido."
                            return@Button
                        }
                        onImportJson(jsonText)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GalaxyCyan)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = DarkBackground)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Importar y Diseñar", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
