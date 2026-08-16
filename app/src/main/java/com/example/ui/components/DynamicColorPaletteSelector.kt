package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

/**
 * DynamicColorPaletteSelector provides real-time granular customization for:
 * 1. Curated full color theme palettes (Cyberpunk, Titanium, Emerald, Royal Gold, Crimson GT, etc.)
 * 2. Fine-grained element colors (Primary Accent, Secondary Accent, Background, Hands, Second Hand, Subdials, Glow)
 * 3. Typography font selector (Galaxy One UI Sans, Orbitron, Roboto Mono, Bebas Neue, Montserrat, Cyber LED, Playfair Serif)
 * 4. Bezel styles and edge borders
 * 5. Hour markers, lume glow effects, and date badges
 */
@Composable
fun DynamicColorPaletteSelector(
    watchFace: WatchFaceEntity,
    onUpdate: ((WatchFaceEntity) -> WatchFaceEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(PaletteSubTab.THEMES) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dynamic_color_palette_selector"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sub-tabs for Color & Styling
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PaletteSubTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) GalaxyCyan.copy(alpha = 0.2f) else DarkSurfaceVariant,
                    animationSpec = tween(200),
                    label = "tabBg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) GalaxyCyan else TextSecondary,
                    animationSpec = tween(200),
                    label = "tabText"
                )

                FilterChip(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    label = {
                        Text(
                            text = tab.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = null,
                            tint = textColor,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = bgColor,
                        labelColor = textColor,
                        selectedContainerColor = bgColor,
                        selectedLabelColor = textColor
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) GalaxyCyan else DarkBorder,
                        selectedBorderColor = GalaxyCyan
                    ),
                    modifier = Modifier.testTag("palette_subtab_${tab.name.lowercase()}")
                )
            }
        }

        when (selectedTab) {
            PaletteSubTab.THEMES -> CuratedThemesSection(watchFace = watchFace, onUpdate = onUpdate)
            PaletteSubTab.CUSTOM_COLORS -> GranularColorPickerSection(watchFace = watchFace, onUpdate = onUpdate)
            PaletteSubTab.TYPOGRAPHY -> TypographySelectorSection(watchFace = watchFace, onUpdate = onUpdate)
            PaletteSubTab.BEZEL_BORDERS -> BezelAndBordersSection(watchFace = watchFace, onUpdate = onUpdate)
        }
    }
}

enum class PaletteSubTab(val displayName: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    THEMES("Temas Completos", Icons.Default.AutoAwesome),
    CUSTOM_COLORS("Colores de Elementos", Icons.Default.ColorLens),
    TYPOGRAPHY("Tipografía & Letras", Icons.Default.TextFields),
    BEZEL_BORDERS("Bordes & Bisel", Icons.Default.DonutLarge)
}

// -------------------------------------------------------------
// 1. THEMES PRESETS SECTION
// -------------------------------------------------------------
@Composable
private fun CuratedThemesSection(
    watchFace: WatchFaceEntity,
    onUpdate: ((WatchFaceEntity) -> WatchFaceEntity) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Paletas de Color Armonizadas en Tiempo Real",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Aplica combinaciones cromáticas de alto contraste y balanceadas en todos los elementos del reloj de un toque.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            fontSize = 12.sp
        )

        ColorThemePresetsCatalog.presets.forEach { preset ->
            val isApplied = watchFace.primaryColor == preset.primaryColor &&
                    watchFace.dialBackgroundColor == preset.dialBackgroundColor

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        onUpdate { current ->
                            current.copy(
                                primaryColor = preset.primaryColor,
                                accentColor = preset.accentColor,
                                dialBackgroundColor = preset.dialBackgroundColor,
                                handsColor = preset.handsColor,
                                secondHandColor = preset.secondHandColor,
                                subdialColor = preset.subdialColor,
                                glowColor = preset.glowColor
                            )
                        }
                    }
                    .testTag("theme_preset_${preset.id}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isApplied) GalaxyCyan.copy(alpha = 0.12f) else DarkSurfaceVariant
                ),
                border = BorderStroke(
                    1.dp,
                    if (isApplied) GalaxyCyan else DarkBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Color swatches row
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(preset.dialBackgroundColor))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(preset.primaryColor))
                        )
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(preset.accentColor))
                        )
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(Color(preset.secondHandColor))
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = preset.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isApplied) GalaxyCyan else TextPrimary
                        )
                        Text(
                            text = preset.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    if (isApplied) {
                        Surface(
                            shape = CircleShape,
                            color = GalaxyCyan,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. GRANULAR COLOR PICKER SECTION
// -------------------------------------------------------------
@Composable
private fun GranularColorPickerSection(
    watchFace: WatchFaceEntity,
    onUpdate: ((WatchFaceEntity) -> WatchFaceEntity) -> Unit
) {
    val vibrantPalettes = listOf(
        0xFF00D2FF to "Cian Eléctrico",
        0xFFFF7A00 to "Naranja Ultra",
        0xFF00FF88 to "Verde Neón",
        0xFFFF2A6D to "Magenta Cyber",
        0xFFFFD700 to "Oro Solar",
        0xFFB026FF to "Violeta Plasma",
        0xFF00E5FF to "Azul Turquesa",
        0xFFFF1744 to "Rojo Carmesí",
        0xFFFFFFFF to "Blanco Nieve",
        0xFF8AB4F8 to "Azul Glaciar",
        0xFFFFE600 to "Amarillo Volt",
        0xFF76FF03 to "Verde Lima"
    )

    val backgroundTones = listOf(
        0xFF000000 to "AMOLED Puro",
        0xFF0B0F17 to "Azul Oscuro",
        0xFF0C1017 to "Titanio Mate",
        0xFF061009 to "Verde Militar",
        0xFF08090D to "Negro Ónix",
        0xFF04111E to "Azul Abisal",
        0xFF0B0B0D to "Grafito Oscuro",
        0xFF0D061A to "Violeta Espacial",
        0xFF1A1A24 to "Gris Carbón",
        0xFF1E1308 to "Bronce Café"
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // A. Primary Accent Color
        ColorSelectorRow(
            title = "Acento Primario (Bisel, Índices, Anillos)",
            selectedColor = watchFace.primaryColor,
            colorOptions = vibrantPalettes,
            onColorSelect = { color ->
                onUpdate { it.copy(primaryColor = color, glowColor = color) }
            }
        )

        // B. Secondary Accent Color
        ColorSelectorRow(
            title = "Acento Secundario & Datos",
            selectedColor = watchFace.accentColor,
            colorOptions = vibrantPalettes,
            onColorSelect = { color ->
                onUpdate { it.copy(accentColor = color) }
            }
        )

        // C. Background Dial Tone
        ColorSelectorRow(
            title = "Fondo de la Esfera (Consumo de Batería)",
            selectedColor = watchFace.dialBackgroundColor,
            colorOptions = backgroundTones,
            onColorSelect = { color ->
                onUpdate { it.copy(dialBackgroundColor = color) }
            }
        )

        // D. Watch Hands Color
        ColorSelectorRow(
            title = "Color de Manecillas (Hora y Minuto)",
            selectedColor = watchFace.handsColor,
            colorOptions = listOf(
                0xFFFFFFFF to "Blanco Nieve",
                0xFFE6EDF5 to "Plata Titanio",
                0xFFFFD700 to "Oro Pulido",
                0xFF00F0FF to "Cian Neón",
                0xFFFF7A00 to "Naranja Sport",
                0xFF00E676 to "Verde Radiactivo",
                0xFFFF1744 to "Rojo Escarlata",
                0xFF1A1A1A to "Negro Stealth"
            ),
            onColorSelect = { color ->
                onUpdate { it.copy(handsColor = color) }
            }
        )

        // E. Second Hand Color
        ColorSelectorRow(
            title = "Color del Segundero",
            selectedColor = watchFace.secondHandColor,
            colorOptions = vibrantPalettes,
            onColorSelect = { color ->
                onUpdate { it.copy(secondHandColor = color) }
            }
        )

        // F. Subdial background
        ColorSelectorRow(
            title = "Fondo de Subesferas y Complicaciones",
            selectedColor = watchFace.subdialColor,
            colorOptions = listOf(
                0xFF101A2D to "Azul Zafiro",
                0xFF192231 to "Gris Titanio",
                0xFF112316 to "Verde Selva",
                0xFF181C26 to "Carbón Suizo",
                0xFF0B253D to "Azul Océano",
                0xFF1A1A22 to "Grafito Mate",
                0xFF21133B to "Púrpura Profundo",
                0xFF000000 to "Negro Transparente"
            ),
            onColorSelect = { color ->
                onUpdate { it.copy(subdialColor = color) }
            }
        )
    }
}

@Composable
private fun ColorSelectorRow(
    title: String,
    selectedColor: Long,
    colorOptions: List<Pair<Long, String>>,
    onColorSelect: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(colorOptions) { (colorLong, label) ->
                val isSelected = selectedColor == colorLong
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onColorSelect(colorLong) }
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(colorLong))
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) GalaxyCyan else DarkBorder,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (colorLong == 0xFFFFFFFFL || colorLong == 0xFFFFD700L || colorLong == 0xFFFFE600L || colorLong == 0xFF76FF03L) Color.Black else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) GalaxyCyan else TextSecondary,
                        fontSize = 9.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. TYPOGRAPHY & LETTERING SECTION
// -------------------------------------------------------------
@Composable
private fun TypographySelectorSection(
    watchFace: WatchFaceEntity,
    onUpdate: ((WatchFaceEntity) -> WatchFaceEntity) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Estilo de Letras & Tipografía del Reloj",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Personaliza la fuente de números de hora, etiquetas de fecha y widgets biométricos.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            fontSize = 12.sp
        )

        WatchFontFamily.values().forEach { font ->
            val isSelected = watchFace.fontFamily == font

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onUpdate { it.copy(fontFamily = font) } }
                    .testTag("font_option_${font.name.lowercase()}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) GalaxyCyan.copy(alpha = 0.12f) else DarkSurfaceVariant
                ),
                border = BorderStroke(
                    1.dp,
                    if (isSelected) GalaxyCyan else DarkBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = font.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) GalaxyCyan else TextPrimary
                        )
                        Text(
                            text = "10:48:32 • 8,420 PASOS • 74 BPM",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(watchFace.primaryColor),
                            fontSize = 13.sp
                        )
                    }

                    RadioButton(
                        selected = isSelected,
                        onClick = { onUpdate { it.copy(fontFamily = font) } },
                        colors = RadioButtonDefaults.colors(selectedColor = GalaxyCyan)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. BEZEL & BORDERS SECTION
// -------------------------------------------------------------
@Composable
private fun BezelAndBordersSection(
    watchFace: WatchFaceEntity,
    onUpdate: ((WatchFaceEntity) -> WatchFaceEntity) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Hour Markers
        Text(
            text = "Estilo de Marcadores de Hora e Índices",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        HourMarkerStyle.values().forEach { marker ->
            val isSelected = watchFace.hourMarkerStyle == marker
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onUpdate { it.copy(hourMarkerStyle = marker) } }
                    .testTag("marker_${marker.name.lowercase()}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) GalaxyCyan.copy(alpha = 0.12f) else DarkSurfaceVariant
                ),
                border = BorderStroke(1.dp, if (isSelected) GalaxyCyan else DarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = marker.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) GalaxyCyan else TextPrimary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    RadioButton(
                        selected = isSelected,
                        onClick = { onUpdate { it.copy(hourMarkerStyle = marker) } },
                        colors = RadioButtonDefaults.colors(selectedColor = GalaxyCyan)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bezel Ring Style
        Text(
            text = "Bisel y Anillo Exterior del Dial",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        BezelStyle.values().forEach { bezel ->
            val isSelected = watchFace.bezelStyle == bezel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onUpdate { it.copy(bezelStyle = bezel) } }
                    .testTag("bezel_${bezel.name.lowercase()}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) GalaxyCyan.copy(alpha = 0.12f) else DarkSurfaceVariant
                ),
                border = BorderStroke(1.dp, if (isSelected) GalaxyCyan else DarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = bezel.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) GalaxyCyan else TextPrimary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                    RadioButton(
                        selected = isSelected,
                        onClick = { onUpdate { it.copy(bezelStyle = bezel) } },
                        colors = RadioButtonDefaults.colors(selectedColor = GalaxyCyan)
                    )
                }
            }
        }

        // Luminescent Glowing Lume Toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            border = BorderStroke(1.dp, DarkBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Efecto de Brillo Super-LumiNova",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Halo brillante en manecillas y marcadores en la oscuridad",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = watchFace.showGlowingLume,
                    onCheckedChange = { checked ->
                        onUpdate { it.copy(showGlowingLume = checked) }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GalaxyCyan,
                        checkedTrackColor = GalaxyCyan.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}
