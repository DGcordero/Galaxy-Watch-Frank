package com.example.ui.screens

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.GalaxyWatchCanvas
import com.example.ui.components.WatchViewMode
import com.example.ui.components.WearableStatusIndicator
import com.example.ui.dialogs.ComplicationDetailDialog
import com.example.ui.dialogs.ComplicationPickerSheet
import com.example.ui.dialogs.ShareExportDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.WatchStudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioEditorScreen(
    viewModel: WatchStudioViewModel,
    modifier: Modifier = Modifier
) {
    val currentWatchFace by viewModel.editingWatchFace.collectAsState()
    val healthData by viewModel.healthSnapshot.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val selectedTab by viewModel.selectedEditorTab.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()

    // Dialog & sheet states
    var activeSlotForPicker by remember { mutableStateOf<ComplicationSlot?>(null) }
    var detailDialogData by remember { mutableStateOf<Pair<ComplicationSlot, ComplicationType>?>(null) }
    var showShareDialog by remember { mutableStateOf(false) }

    val watchFace = currentWatchFace ?: return

    val tabTitles = listOf("Estilo", "Manecillas", "Complicaciones", "Colores", "Efectos & AOD")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = watchFace.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    WearableStatusIndicator(
                        status = connectionStatus,
                        compact = true
                    )
                }
                Text(
                    text = "${watchFace.category} • One UI 6 Watch",
                    style = MaterialTheme.typography.bodySmall,
                    color = GalaxyCyan
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // View Mode Toggle (Active / AOD / Night)
                IconButton(
                    onClick = {
                        val nextMode = when (viewMode) {
                            WatchViewMode.ACTIVE -> WatchViewMode.ALWAYS_ON_DISPLAY
                            WatchViewMode.ALWAYS_ON_DISPLAY -> WatchViewMode.NIGHT_RED_SHIFT
                            WatchViewMode.NIGHT_RED_SHIFT -> WatchViewMode.ACTIVE
                        }
                        viewModel.setViewMode(nextMode)
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            when (viewMode) {
                                WatchViewMode.ALWAYS_ON_DISPLAY -> GalaxyAmber.copy(alpha = 0.2f)
                                WatchViewMode.NIGHT_RED_SHIFT -> Color(0xFFFF2A2A).copy(alpha = 0.2f)
                                WatchViewMode.ACTIVE -> DarkSurfaceVariant
                            }
                        )
                ) {
                    Icon(
                        imageVector = when (viewMode) {
                            WatchViewMode.ALWAYS_ON_DISPLAY -> Icons.Default.BrightnessMedium
                            WatchViewMode.NIGHT_RED_SHIFT -> Icons.Default.Visibility
                            WatchViewMode.ACTIVE -> Icons.Default.WbSunny
                        },
                        contentDescription = "Modo de visualización",
                        tint = when (viewMode) {
                            WatchViewMode.ALWAYS_ON_DISPLAY -> GalaxyAmber
                            WatchViewMode.NIGHT_RED_SHIFT -> Color(0xFFFF2A2A)
                            WatchViewMode.ACTIVE -> GalaxyCyan
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Share / Export Button
                IconButton(
                    onClick = { showShareDialog = true },
                    modifier = Modifier.clip(CircleShape).background(DarkSurfaceVariant)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Compartir", tint = TextPrimary, modifier = Modifier.size(20.dp))
                }

                // Instant Sync Button
                FilledTonalButton(
                    onClick = { viewModel.applyAndSyncActiveWatchFace(watchFace) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = GalaxyUltraOrange,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("sync_watch_button")
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sincronizar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Circular Live Watch Face Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            // Galaxy Watch Bezel frame shadow
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .clip(CircleShape)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(Color(0xFF222B3D), Color(0xFF131722), Color(0xFF090B10))
                        )
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                GalaxyWatchCanvas(
                    watchFace = watchFace,
                    healthData = healthData,
                    viewMode = viewMode,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    onComplicationClick = { slot, type ->
                        detailDialogData = slot to type
                    }
                )
            }
        }

        // View Mode Indicator Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (viewMode) {
                    WatchViewMode.ACTIVE -> "● Modo Activo (60 FPS)"
                    WatchViewMode.ALWAYS_ON_DISPLAY -> "🌙 AOD Bajo Consumo (~8% OPR)"
                    WatchViewMode.NIGHT_RED_SHIFT -> "🔴 Visión Nocturna Red-Shift"
                },
                style = MaterialTheme.typography.labelSmall,
                color = when (viewMode) {
                    WatchViewMode.ACTIVE -> GalaxyEmerald
                    WatchViewMode.ALWAYS_ON_DISPLAY -> GalaxyAmber
                    WatchViewMode.NIGHT_RED_SHIFT -> Color(0xFFFF4444)
                },
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Toca las métricas para ver detalles",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }

        // Tab Row for Customization
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurface,
            contentColor = GalaxyCyan,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = GalaxyCyan,
                    height = 3.dp
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { viewModel.setEditorTab(index) },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        // Customization Content according to selected tab
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(DarkSurfaceVariant.copy(alpha = 0.5f))
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTab) {
                0 -> StyleTabContent(watchFace = watchFace, onUpdate = viewModel::updateEditedWatchFace)
                1 -> HandsTabContent(watchFace = watchFace, onUpdate = viewModel::updateEditedWatchFace)
                2 -> ComplicationsTabContent(
                    watchFace = watchFace,
                    healthData = healthData,
                    onOpenSlotPicker = { slot -> activeSlotForPicker = slot },
                    onOpenDetail = { slot, type -> detailDialogData = slot to type }
                )
                3 -> ColorsAndFontsTabContent(watchFace = watchFace, onUpdate = viewModel::updateEditedWatchFace)
                4 -> EffectsAndAodTabContent(watchFace = watchFace, onUpdate = viewModel::updateEditedWatchFace)
            }
        }
    }

    // Complication Picker Bottom Sheet
    if (activeSlotForPicker != null) {
        val slot = activeSlotForPicker!!
        val currentType = when (slot) {
            ComplicationSlot.TOP -> ComplicationType.valueOf(watchFace.complicationTop)
            ComplicationSlot.BOTTOM -> ComplicationType.valueOf(watchFace.complicationBottom)
            ComplicationSlot.LEFT -> ComplicationType.valueOf(watchFace.complicationLeft)
            ComplicationSlot.RIGHT -> ComplicationType.valueOf(watchFace.complicationRight)
            ComplicationSlot.CENTER -> ComplicationType.valueOf(watchFace.complicationCenter)
        }

        ComplicationPickerSheet(
            slot = slot,
            currentType = currentType,
            onSelectType = { selectedType ->
                viewModel.updateEditedWatchFace { face ->
                    when (slot) {
                        ComplicationSlot.TOP -> face.copy(complicationTop = selectedType.name)
                        ComplicationSlot.BOTTOM -> face.copy(complicationBottom = selectedType.name)
                        ComplicationSlot.LEFT -> face.copy(complicationLeft = selectedType.name)
                        ComplicationSlot.RIGHT -> face.copy(complicationRight = selectedType.name)
                        ComplicationSlot.CENTER -> face.copy(complicationCenter = selectedType.name)
                    }
                }
            },
            onDismiss = { activeSlotForPicker = null }
        )
    }

    // Complication Detail Dialog
    if (detailDialogData != null) {
        val (slot, type) = detailDialogData!!
        ComplicationDetailDialog(
            slot = slot,
            type = type,
            healthData = healthData,
            onAdjustMetric = { delta -> viewModel.updateHealthMetric(type, delta) },
            onDismiss = { detailDialogData = null }
        )
    }

    // Share / Export Dialog
    if (showShareDialog) {
        ShareExportDialog(
            watchFace = watchFace,
            repository = viewModel.repository,
            onDismiss = { showShareDialog = false }
        )
    }
}

// ---------------- TAB 0: Estilo & Base ----------------
@Composable
private fun StyleTabContent(
    watchFace: WatchFaceEntity,
    onUpdate: ((WatchFaceEntity) -> WatchFaceEntity) -> Unit
) {
    // Watch Face Name Input
    OutlinedTextField(
        value = watchFace.title,
        onValueChange = { newTitle -> onUpdate { it.copy(title = newTitle) } },
        label = { Text("Nombre de la Esfera") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GalaxyCyan,
            unfocusedBorderColor = DarkBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        )
    )

    // Dial Type selector
    Text("Tipo de Esfera / Mecanismo", style = MaterialTheme.typography.labelLarge, color = GalaxyCyan, fontWeight = FontWeight.Bold)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(WatchDialType.values()) { dial ->
            val isSelected = watchFace.dialType == dial
            FilterChip(
                selected = isSelected,
                onClick = { onUpdate { it.copy(dialType = dial) } },
                label = { Text(dial.displayName, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GalaxyCyan.copy(alpha = 0.2f),
                    selectedLabelColor = GalaxyCyan,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    selectedBorderColor = GalaxyCyan,
                    borderColor = DarkBorder
                )
            )
        }
    }

    // Background Texture Pattern
    Text("Textura de Fondo", style = MaterialTheme.typography.labelLarge, color = GalaxyCyan, fontWeight = FontWeight.Bold)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(WatchBackgroundPattern.values()) { bg ->
            val isSelected = watchFace.backgroundPattern == bg
            FilterChip(
                selected = isSelected,
                onClick = { onUpdate { it.copy(backgroundPattern = bg) } },
                label = { Text(bg.displayName, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GalaxyUltraOrange.copy(alpha = 0.2f),
                    selectedLabelColor = GalaxyUltraOrange,
                    labelColor = TextSecondary
                )
            )
        }
    }

    // Bezel Style
    Text("Bisel Giratorio / Escala Exterior", style = MaterialTheme.typography.labelLarge, color = GalaxyCyan, fontWeight = FontWeight.Bold)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(BezelStyle.values()) { bezel ->
            val isSelected = watchFace.bezelStyle == bezel
            FilterChip(
                selected = isSelected,
                onClick = { onUpdate { it.copy(bezelStyle = bezel) } },
                label = { Text(bezel.displayName, fontSize = 12.sp) }
            )
        }
    }

    // Hour Markers
    Text("Marcadores de Hora", style = MaterialTheme.typography.labelLarge, color = GalaxyCyan, fontWeight = FontWeight.Bold)
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(HourMarkerStyle.values()) { marker ->
            val isSelected = watchFace.hourMarkerStyle == marker
            FilterChip(
                selected = isSelected,
                onClick = { onUpdate { it.copy(hourMarkerStyle = marker) } },
                label = { Text(marker.displayName, fontSize = 12.sp) }
            )
        }
    }

    // Date Badge Toggle
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Insignia de Fecha", style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
            Text("Muestra día y mes en la esfera", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        Switch(
            checked = watchFace.showDateBadge,
            onCheckedChange = { onUpdate { f -> f.copy(showDateBadge = it) } },
            colors = SwitchDefaults.colors(checkedThumbColor = GalaxyCyan, checkedTrackColor = GalaxyCyan.copy(alpha = 0.3f))
        )
    }
}

// ---------------- TAB 1: Manecillas (Hands) ----------------
@Composable
private fun HandsTabContent(
    watchFace: WatchFaceEntity,
    onUpdate: ((WatchFaceEntity) -> WatchFaceEntity) -> Unit
) {
    Text("Estilo de Manecillas Intercambiables", style = MaterialTheme.typography.labelLarge, color = GalaxyCyan, fontWeight = FontWeight.Bold)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        WatchHandStyle.values().forEach { style ->
            val isSelected = watchFace.handStyle == style
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onUpdate { it.copy(handStyle = style) } },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) GalaxyCyan.copy(alpha = 0.15f) else DarkSurface
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(style.displayName, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(style.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    if (isSelected) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GalaxyCyan)
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Text("Movimiento del Segundero", style = MaterialTheme.typography.labelLarge, color = GalaxyCyan, fontWeight = FontWeight.Bold)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SecondHandMovement.values().forEach { mov ->
            val isSelected = watchFace.secondHandMovement == mov
            FilterChip(
                selected = isSelected,
                onClick = { onUpdate { it.copy(secondHandMovement = mov) } },
                label = { Text(mov.displayName, fontSize = 11.sp) }
            )
        }
    }
}

// ---------------- TAB 2: Complicaciones & Salud ----------------
@Composable
private fun ComplicationsTabContent(
    watchFace: WatchFaceEntity,
    healthData: GalaxyHealthSnapshot,
    onOpenSlotPicker: (ComplicationSlot) -> Unit,
    onOpenDetail: (ComplicationSlot, ComplicationType) -> Unit
) {
    Text("Ranuras de Complicaciones Dinámicas", style = MaterialTheme.typography.labelLarge, color = GalaxyCyan, fontWeight = FontWeight.Bold)
    Text(
        text = "Asigna métricas de salud y datos en vivo a cada posición de tu Galaxy Watch:",
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary
    )

    val slots = listOf(
        ComplicationSlot.TOP to watchFace.complicationTop,
        ComplicationSlot.BOTTOM to watchFace.complicationBottom,
        ComplicationSlot.LEFT to watchFace.complicationLeft,
        ComplicationSlot.RIGHT to watchFace.complicationRight,
        ComplicationSlot.CENTER to watchFace.complicationCenter
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        slots.forEach { (slot, typeName) ->
            val type = try {
                ComplicationType.valueOf(typeName)
            } catch (e: Exception) {
                ComplicationType.NONE
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GalaxyCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = com.example.ui.dialogs.getComplicationIcon(type),
                                contentDescription = null,
                                tint = GalaxyCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Column {
                            Text(slot.displayName, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 13.sp)
                            Text(type.title, style = MaterialTheme.typography.bodySmall, color = GalaxyUltraOrange)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(onClick = { onOpenDetail(slot, type) }) {
                            Icon(Icons.Default.Info, contentDescription = "Detalles", tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                        Button(
                            onClick = { onOpenSlotPicker(slot) },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkBorder),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Cambiar", fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

// ---------------- TAB 3: Colores & Fuentes ----------------
@Composable
private fun ColorsAndFontsTabContent(
    watchFace: WatchFaceEntity,
    onUpdate: ((WatchFaceEntity) -> WatchFaceEntity) -> Unit
) {
    Text("Paletas Curadas de Galaxy S25 & One UI", style = MaterialTheme.typography.labelLarge, color = GalaxyCyan, fontWeight = FontWeight.Bold)

    val colorPresets = listOf(
        "Ultra Cyan & Orange" to (0xFF00D2FF to 0xFFFF7A00),
        "Emerald Health" to (0xFF00E676 to 0xFFFFD700),
        "Cyberpunk Neon" to (0xFFFF0055 to 0xFF00F0FF),
        "Royal Gold & Bronze" to (0xFFFFD700 to 0xFF9E7B35),
        "Titanium Silver" to (0xFFE2E8F0 to 0xFF2D7DFA),
        "Deep Crimson Sport" to (0xFFFF1744 to 0xFFFF9100),
        "Pastel Minimal" to (0xFF8AB4F8 to 0xFFFF8A65)
    )

    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(colorPresets) { (name, pair) ->
            val isSelected = watchFace.primaryColor == pair.first
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onUpdate {
                            it.copy(
                                primaryColor = pair.first,
                                accentColor = pair.second,
                                secondHandColor = pair.second,
                                glowColor = pair.first
                            )
                        }
                    }
                    .background(if (isSelected) GalaxyCyan.copy(alpha = 0.2f) else DarkSurface)
                    .padding(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(Color(pair.first)))
                    Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(Color(pair.second)))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(name, fontSize = 10.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text("Familia Tipográfica", style = MaterialTheme.typography.labelLarge, color = GalaxyCyan, fontWeight = FontWeight.Bold)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        WatchFontFamily.values().forEach { font ->
            val isSelected = watchFace.fontFamily == font
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onUpdate { it.copy(fontFamily = font) } },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) GalaxyCyan.copy(alpha = 0.15f) else DarkSurface
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(font.displayName, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("12:45", fontWeight = FontWeight.Bold, color = GalaxyCyan, fontSize = 16.sp)
                }
            }
        }
    }
}

// ---------------- TAB 4: Efectos & AOD ----------------
@Composable
private fun EffectsAndAodTabContent(
    watchFace: WatchFaceEntity,
    onUpdate: ((WatchFaceEntity) -> WatchFaceEntity) -> Unit
) {
    Text("Configuración de Always-On Display (AOD)", style = MaterialTheme.typography.labelLarge, color = GalaxyCyan, fontWeight = FontWeight.Bold)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Optimización Anti Burn-In", fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Desplaza micro-píxeles cada 60s en One UI", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
                Switch(
                    checked = watchFace.aodOptimized,
                    onCheckedChange = { onUpdate { f -> f.copy(aodOptimized = it) } },
                    colors = SwitchDefaults.colors(checkedThumbColor = GalaxyEmerald)
                )
            }

            Divider(color = DarkBorder)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Ratio de Píxeles AOD (OPR):", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text("${watchFace.aodPixelPercentage}% (Excelente)", fontWeight = FontWeight.Bold, color = GalaxyEmerald)
            }

            LinearProgressIndicator(
                progress = { watchFace.aodPixelPercentage / 100f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = GalaxyEmerald,
                trackColor = DarkBorder
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text("Efectos Luminiscentes y Super-LumiNova", style = MaterialTheme.typography.labelLarge, color = GalaxyCyan, fontWeight = FontWeight.Bold)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Brillo Luminous en Manecillas", fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Efecto de resplandor radiactivo nocturno", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Switch(
            checked = watchFace.showGlowingLume,
            onCheckedChange = { onUpdate { f -> f.copy(showGlowingLume = it) } },
            colors = SwitchDefaults.colors(checkedThumbColor = GalaxyCyan)
        )
    }
}
