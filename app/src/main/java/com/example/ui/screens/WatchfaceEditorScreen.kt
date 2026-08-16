package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.GalaxyWatchCanvas
import com.example.ui.components.DynamicColorPaletteSelector
import com.example.ui.components.TemplateSelectorSection
import com.example.ui.components.WatchViewMode
import com.example.ui.components.WearableStatusIndicator
import com.example.ui.dialogs.ComplicationDetailDialog
import com.example.ui.dialogs.ComplicationPickerSheet
import com.example.ui.dialogs.ShareExportDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.WatchStudioViewModel

enum class EditorSection(val title: String, val icon: ImageVector) {
    TEMPLATES("Plantillas Pro", Icons.Filled.AutoAwesome),
    PALETTES("Paletas & Estilos", Icons.Filled.ColorLens),
    BACKGROUND("Fondo & Textura", Icons.Filled.Palette),
    HANDS("Manecillas", Icons.Filled.WatchLater),
    COMPLICATIONS("Complicaciones", Icons.Filled.Widgets),
    DIAL_BEZEL("Esfera & Bisel", Icons.Filled.Timelapse)
}

/**
 * WatchfaceEditorScreen displays a circular watch preview area paired with
 * an interactive side-panel for customizing watch face elements like background color,
 * clock hands, dial patterns, and complications.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchfaceEditorScreen(
    viewModel: WatchStudioViewModel,
    modifier: Modifier = Modifier
) {
    val currentWatchFace by viewModel.editingWatchFace.collectAsState()
    val healthData by viewModel.healthSnapshot.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()

    var activeSection by remember { mutableStateOf(EditorSection.BACKGROUND) }
    var activeSlotForPicker by remember { mutableStateOf<ComplicationSlot?>(null) }
    var detailDialogData by remember { mutableStateOf<Pair<ComplicationSlot, ComplicationType>?>(null) }
    var showShareDialog by remember { mutableStateOf(false) }

    val watchFace = currentWatchFace ?: return

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("watchface_editor_screen")
    ) {
        val isWideScreen = maxWidth >= 680.dp

        Column(modifier = Modifier.fillMaxSize()) {
            // Top Header Bar
            EditorHeader(
                watchFace = watchFace,
                viewMode = viewMode,
                connectionStatus = connectionStatus,
                onToggleViewMode = {
                    val nextMode = when (viewMode) {
                        WatchViewMode.ACTIVE -> WatchViewMode.ALWAYS_ON_DISPLAY
                        WatchViewMode.ALWAYS_ON_DISPLAY -> WatchViewMode.NIGHT_RED_SHIFT
                        WatchViewMode.NIGHT_RED_SHIFT -> WatchViewMode.ACTIVE
                    }
                    viewModel.setViewMode(nextMode)
                },
                onExport = { showShareDialog = true },
                onSync = { viewModel.applyAndSyncActiveWatchFace(watchFace) }
            )

            if (isWideScreen) {
                // Dual Pane Horizontal Layout: Circular Preview on Left, Side-Panel on Right
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Left Column: Circular Watch Preview Area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularPreviewContainer(
                            watchFace = watchFace,
                            healthData = healthData,
                            viewMode = viewMode,
                            onComplicationClick = { slot, type ->
                                detailDialogData = Pair(slot, type)
                            }
                        )
                    }

                    // Right Column: Side-Panel for Customization
                    Card(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                            .testTag("editor_side_panel"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Side-Panel Navigation Tabs
                            SidePanelTabRow(
                                activeSection = activeSection,
                                onSectionSelected = { activeSection = it }
                            )

                            HorizontalDivider(color = DarkBorder, thickness = 1.dp)

                            // Side-Panel Content Sections
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp)
                            ) {
                                when (activeSection) {
                                    EditorSection.TEMPLATES -> TemplateSelectorSection(
                                        currentWatchFace = watchFace,
                                        healthData = healthData,
                                        onApplyTemplate = { newFace ->
                                            viewModel.updateEditedWatchFace { newFace }
                                        }
                                    )
                                    EditorSection.PALETTES -> DynamicColorPaletteSelector(
                                        watchFace = watchFace,
                                        onUpdate = { viewModel.updateEditedWatchFace(it) }
                                    )
                                    EditorSection.BACKGROUND -> BackgroundCustomizer(
                                        watchFace = watchFace,
                                        onUpdate = { viewModel.updateEditedWatchFace(it) }
                                    )
                                    EditorSection.HANDS -> HandsCustomizer(
                                        watchFace = watchFace,
                                        onUpdate = { viewModel.updateEditedWatchFace(it) }
                                    )
                                    EditorSection.COMPLICATIONS -> ComplicationsCustomizer(
                                        watchFace = watchFace,
                                        onSelectSlot = { activeSlotForPicker = it }
                                    )
                                    EditorSection.DIAL_BEZEL -> DialAndBezelCustomizer(
                                        watchFace = watchFace,
                                        onUpdate = { viewModel.updateEditedWatchFace(it) }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Compact Vertical Layout: Circular Preview at top, Customizer Panel below
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Circular Preview Section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularPreviewContainer(
                            watchFace = watchFace,
                            healthData = healthData,
                            viewMode = viewMode,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 320.dp),
                            onComplicationClick = { slot, type ->
                                detailDialogData = Pair(slot, type)
                            }
                        )
                    }

                    // Side-Panel in Form of Bottom Customization Container
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_side_panel"),
                        color = DarkSurface,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        tonalElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp)
                        ) {
                            SidePanelTabRow(
                                activeSection = activeSection,
                                onSectionSelected = { activeSection = it }
                            )

                            HorizontalDivider(color = DarkBorder, thickness = 1.dp)

                            Box(modifier = Modifier.padding(16.dp)) {
                                when (activeSection) {
                                    EditorSection.TEMPLATES -> TemplateSelectorSection(
                                        currentWatchFace = watchFace,
                                        healthData = healthData,
                                        onApplyTemplate = { newFace ->
                                            viewModel.updateEditedWatchFace { newFace }
                                        }
                                    )
                                    EditorSection.PALETTES -> DynamicColorPaletteSelector(
                                        watchFace = watchFace,
                                        onUpdate = { viewModel.updateEditedWatchFace(it) }
                                    )
                                    EditorSection.BACKGROUND -> BackgroundCustomizer(
                                        watchFace = watchFace,
                                        onUpdate = { viewModel.updateEditedWatchFace(it) }
                                    )
                                    EditorSection.HANDS -> HandsCustomizer(
                                        watchFace = watchFace,
                                        onUpdate = { viewModel.updateEditedWatchFace(it) }
                                    )
                                    EditorSection.COMPLICATIONS -> ComplicationsCustomizer(
                                        watchFace = watchFace,
                                        onSelectSlot = { activeSlotForPicker = it }
                                    )
                                    EditorSection.DIAL_BEZEL -> DialAndBezelCustomizer(
                                        watchFace = watchFace,
                                        onUpdate = { viewModel.updateEditedWatchFace(it) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Complication Picker Sheet
    activeSlotForPicker?.let { targetSlot ->
        val currentSlotType = try {
            val typeStr = when (targetSlot) {
                ComplicationSlot.TOP -> watchFace.complicationTop
                ComplicationSlot.BOTTOM -> watchFace.complicationBottom
                ComplicationSlot.LEFT -> watchFace.complicationLeft
                ComplicationSlot.RIGHT -> watchFace.complicationRight
                ComplicationSlot.CENTER -> watchFace.complicationCenter
            }
            ComplicationType.valueOf(typeStr)
        } catch (e: Exception) {
            ComplicationType.NONE
        }

        ComplicationPickerSheet(
            slot = targetSlot,
            currentType = currentSlotType,
            onSelectType = { newType ->
                viewModel.updateEditedWatchFace { face ->
                    when (targetSlot) {
                        ComplicationSlot.TOP -> face.copy(complicationTop = newType.name)
                        ComplicationSlot.BOTTOM -> face.copy(complicationBottom = newType.name)
                        ComplicationSlot.LEFT -> face.copy(complicationLeft = newType.name)
                        ComplicationSlot.RIGHT -> face.copy(complicationRight = newType.name)
                        ComplicationSlot.CENTER -> face.copy(complicationCenter = newType.name)
                    }
                }
                activeSlotForPicker = null
            },
            onDismiss = { activeSlotForPicker = null }
        )
    }

    // Complication Detail Dialog
    detailDialogData?.let { (slot, compType) ->
        ComplicationDetailDialog(
            slot = slot,
            type = compType,
            healthData = healthData,
            onAdjustMetric = { /* Real-time simulated tweak */ },
            onDismiss = { detailDialogData = null }
        )
    }

    // Share & WFF Export Dialog
    if (showShareDialog) {
        ShareExportDialog(
            watchFace = watchFace,
            repository = viewModel.repository,
            onDismiss = { showShareDialog = false }
        )
    }
}

// -------------------------------------------------------------
// UI SUBCOMPONENTS
// -------------------------------------------------------------

@Composable
private fun EditorHeader(
    watchFace: WatchFaceEntity,
    viewMode: WatchViewMode,
    connectionStatus: ConnectionStatus,
    onToggleViewMode: () -> Unit,
    onExport: () -> Unit,
    onSync: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                WearableStatusIndicator(
                    status = connectionStatus,
                    compact = true
                )
            }
            Text(
                text = "${watchFace.dialType.displayName} • Galaxy Watch Studio",
                style = MaterialTheme.typography.bodySmall,
                color = GalaxyCyan
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            // View mode switch (Active, AOD, Night)
            IconButton(
                onClick = onToggleViewMode,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        when (viewMode) {
                            WatchViewMode.ALWAYS_ON_DISPLAY -> GalaxyAmber.copy(alpha = 0.2f)
                            WatchViewMode.NIGHT_RED_SHIFT -> Color(0xFFFF2A2A).copy(alpha = 0.2f)
                            WatchViewMode.ACTIVE -> DarkSurfaceVariant
                        }
                    )
                    .testTag("toggle_view_mode_btn")
            ) {
                Icon(
                    imageVector = when (viewMode) {
                        WatchViewMode.ALWAYS_ON_DISPLAY -> Icons.Filled.Bedtime
                        WatchViewMode.NIGHT_RED_SHIFT -> Icons.Filled.Visibility
                        WatchViewMode.ACTIVE -> Icons.Filled.WbSunny
                    },
                    contentDescription = "Cambiar modo de visualización",
                    tint = when (viewMode) {
                        WatchViewMode.ALWAYS_ON_DISPLAY -> GalaxyAmber
                        WatchViewMode.NIGHT_RED_SHIFT -> Color(0xFFFF4D4D)
                        WatchViewMode.ACTIVE -> GalaxyCyan
                    }
                )
            }

            // Export WFF
            IconButton(
                onClick = onExport,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
                    .testTag("export_wff_btn")
            ) {
                Icon(
                    imageVector = Icons.Filled.FileDownload,
                    contentDescription = "Exportar WFF",
                    tint = TextSecondary
                )
            }

            // Sync to Galaxy Watch
            Button(
                onClick = onSync,
                colors = ButtonDefaults.buttonColors(containerColor = GalaxyUltraOrange),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.testTag("sync_watch_btn")
            ) {
                Icon(
                    imageVector = Icons.Filled.Sync,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Sincronizar",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Circular Watch Preview Area styled after the Samsung Galaxy Watch Ultra Titanium Case
 */
@Composable
fun CircularPreviewContainer(
    watchFace: WatchFaceEntity,
    healthData: GalaxyHealthSnapshot,
    viewMode: WatchViewMode,
    modifier: Modifier = Modifier,
    onComplicationClick: (ComplicationSlot, ComplicationType) -> Unit
) {
    Box(
        modifier = modifier
            .padding(12.dp)
            .testTag("circular_preview_container"),
        contentAlignment = Alignment.Center
    ) {
        // Physical Watch Cushion Case Frame (Galaxy Watch Ultra Titanium Cushion Shape)
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .fillMaxSize(0.96f)
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(42.dp), ambientColor = Color.Black, spotColor = GalaxyCyan.copy(alpha = 0.3f))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF2C3240),
                            Color(0xFF161922),
                            Color(0xFF0D0F14)
                        )
                    ),
                    shape = RoundedCornerShape(42.dp)
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFF5A6680),
                            Color(0xFF2A303D),
                            Color(0xFF14171E)
                        )
                    ),
                    shape = RoundedCornerShape(42.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            // Galaxy Watch Ultra Quick Button Accent (Orange Accent on right side)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 6.dp)
                    .size(width = 8.dp, height = 36.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(GalaxyUltraOrange)
            )

            // Inner Circular Titanium Bezel
            Box(
                modifier = Modifier
                    .fillMaxSize(0.92f)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .border(
                        width = 4.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                Color(0xFF3E4758),
                                Color(0xFF1E232D),
                                Color(0xFF5A6680),
                                Color(0xFF1E232D),
                                Color(0xFF3E4758)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // The Real-Time Circular Watch Face Canvas
                GalaxyWatchCanvas(
                    watchFace = watchFace,
                    healthData = healthData,
                    viewMode = viewMode,
                    modifier = Modifier.fillMaxSize(),
                    onComplicationClick = onComplicationClick
                )
            }
        }
    }
}

/**
 * Tab row to switch categories in the Side-Panel
 */
@Composable
private fun SidePanelTabRow(
    activeSection: EditorSection,
    onSectionSelected: (EditorSection) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        EditorSection.values().forEach { section ->
            val isSelected = activeSection == section
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) GalaxyCyan.copy(alpha = 0.18f) else DarkSurfaceVariant,
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
                onClick = { onSectionSelected(section) },
                label = {
                    Text(
                        text = section.title,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = section.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = textColor
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
                    borderColor = if (isSelected) GalaxyCyan else Color.Transparent,
                    selectedBorderColor = GalaxyCyan
                ),
                modifier = Modifier.testTag("editor_tab_${section.name.lowercase()}")
            )
        }
    }
}

// -------------------------------------------------------------
// SIDE-PANEL SECTION 1: BACKGROUND & COLOR CUSTOMIZER
// -------------------------------------------------------------

@Composable
fun BackgroundCustomizer(
    watchFace: WatchFaceEntity,
    onUpdate: ((WatchFaceEntity) -> WatchFaceEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("section_background_customizer"),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Dial Background Color Swatches
        Text(
            text = "Color de Fondo de Esfera",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        val bgColors = listOf(
            0xFF0B0F17 to "OLED Negro",
            0xFF0F172A to "Azul Noche",
            0xFF18181B to "Titanio Mate",
            0xFF1A0B2E to "Cyber Violeta",
            0xFF06281E to "Verde Táctico",
            0xFF2B0A0A to "Rojo Profundo",
            0xFF1C1917 to "Carbón Piedra",
            0xFF0A192F to "Abisal"
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(bgColors) { (colorLong, label) ->
                val isSelected = watchFace.dialBackgroundColor == colorLong
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onUpdate { it.copy(dialBackgroundColor = colorLong) }
                        }
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(colorLong))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) GalaxyCyan else DarkBorder,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) GalaxyCyan else TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // 2. Primary Accent Colors
        Text(
            text = "Color de Acento Principal",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        val accentColors = listOf(
            0xFF00D2FF to "Galaxy Cyan",
            0xFFFF7A00 to "Ultra Naranja",
            0xFF00FF88 to "Verde Neón",
            0xFFFF2A6D to "Cyber Rosa",
            0xFFFFD700 to "Oro Solar",
            0xFFB026FF to "Púrpura",
            0xFFFFFFFF to "Blanco Nieve",
            0xFF00E5FF to "Aqua Pro"
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(accentColors) { (colorLong, label) ->
                val isSelected = watchFace.primaryColor == colorLong
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onUpdate { it.copy(primaryColor = colorLong, glowColor = colorLong) }
                        }
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(colorLong))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = if (colorLong == 0xFFFFFFFFL || colorLong == 0xFFFFD700L) Color.Black else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) GalaxyCyan else TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // 3. Background Pattern
        Text(
            text = "Textura & Patrón de Fondo",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        WatchBackgroundPattern.values().forEach { pattern ->
            val isSelected = watchFace.backgroundPattern == pattern
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onUpdate { it.copy(backgroundPattern = pattern) }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) GalaxyCyan.copy(alpha = 0.12f) else DarkSurfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) GalaxyCyan else DarkBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = pattern.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) GalaxyCyan else TextPrimary
                    )
                    RadioButton(
                        selected = isSelected,
                        onClick = { onUpdate { it.copy(backgroundPattern = pattern) } },
                        colors = RadioButtonDefaults.colors(selectedColor = GalaxyCyan)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SIDE-PANEL SECTION 2: CLOCK HANDS CUSTOMIZER
// -------------------------------------------------------------

@Composable
fun HandsCustomizer(
    watchFace: WatchFaceEntity,
    onUpdate: ((WatchFaceEntity) -> WatchFaceEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("section_hands_customizer"),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Hand Style Selection
        Text(
            text = "Diseño de Manecillas (Hora y Minuto)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        WatchHandStyle.values().forEach { style ->
            val isSelected = watchFace.handStyle == style
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        onUpdate { it.copy(handStyle = style) }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) GalaxyCyan.copy(alpha = 0.14f) else DarkSurfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) GalaxyCyan else DarkBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = style.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) GalaxyCyan else TextPrimary
                        )
                        Text(
                            text = style.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    RadioButton(
                        selected = isSelected,
                        onClick = { onUpdate { it.copy(handStyle = style) } },
                        colors = RadioButtonDefaults.colors(selectedColor = GalaxyCyan)
                    )
                }
            }
        }

        // 2. Second Hand Movement (Smooth vs Tick)
        Text(
            text = "Movimiento del Segundero",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SecondHandMovement.values().forEach { movement ->
                val isSelected = watchFace.secondHandMovement == movement
                OutlinedButton(
                    onClick = { onUpdate { it.copy(secondHandMovement = movement) } },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) GalaxyCyan.copy(alpha = 0.15f) else Color.Transparent
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) GalaxyCyan else DarkBorder
                    )
                ) {
                    Text(
                        text = when (movement) {
                            SecondHandMovement.SWEEP_60FPS -> "Barrido 60fps"
                            SecondHandMovement.TICK_1HZ -> "Salto 1Hz"
                            SecondHandMovement.HIDDEN -> "Oculto"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) GalaxyCyan else TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 3. Second Hand Color
        Text(
            text = "Color del Segundero",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        val secondHandColors = listOf(
            0xFFFF7A00 to "Naranja Ultra",
            0xFFFF2A2A to "Rojo Carrera",
            0xFF00D2FF to "Cyan Neón",
            0xFFFFD700 to "Amarillo Oro",
            0xFF00FF88 to "Verde Lima",
            0xFFFFFFFF to "Blanco Puro"
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(secondHandColors) { (colorLong, label) ->
                val isSelected = watchFace.secondHandColor == colorLong
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onUpdate { it.copy(secondHandColor = colorLong) }
                        }
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(colorLong))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = if (colorLong == 0xFFFFFFFFL || colorLong == 0xFFFFD700L) Color.Black else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) GalaxyCyan else TextSecondary,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // 4. Lume / Night Glow Effect
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
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
                        text = "Efecto Super-LumiNova Nocturno",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Brillo de alta intensidad en agujas y marcadores en oscuridad",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Switch(
                    checked = watchFace.showGlowingLume,
                    onCheckedChange = { isChecked ->
                        onUpdate { it.copy(showGlowingLume = isChecked) }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GalaxyCyan,
                        checkedTrackColor = GalaxyCyan.copy(alpha = 0.4f)
                    )
                )
            }
        }
    }
}

// -------------------------------------------------------------
// SIDE-PANEL SECTION 3: COMPLICATIONS CUSTOMIZER
// -------------------------------------------------------------

@Composable
fun ComplicationsCustomizer(
    watchFace: WatchFaceEntity,
    onSelectSlot: (ComplicationSlot) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("section_complications_customizer"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Ranuras de Complicaciones",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Toca cada posición para asignar sensores de salud, batería o clima.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )

        val slots = listOf(
            Triple(ComplicationSlot.TOP, "Ranura Superior (12h)", watchFace.complicationTop),
            Triple(ComplicationSlot.BOTTOM, "Ranura Inferior (6h)", watchFace.complicationBottom),
            Triple(ComplicationSlot.LEFT, "Ranura Izquierda (9h)", watchFace.complicationLeft),
            Triple(ComplicationSlot.RIGHT, "Ranura Derecha (3h)", watchFace.complicationRight),
            Triple(ComplicationSlot.CENTER, "Ranura Central", watchFace.complicationCenter)
        )

        slots.forEach { (slot, title, typeName) ->
            val compType = try {
                ComplicationType.valueOf(typeName)
            } catch (e: Exception) {
                ComplicationType.NONE
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelectSlot(slot) }
                    .testTag("slot_${slot.name.lowercase()}"),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(GalaxyCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (compType) {
                                ComplicationType.STEPS -> Icons.Filled.DirectionsWalk
                                ComplicationType.HEART_RATE -> Icons.Filled.Favorite
                                ComplicationType.BATTERY_WATCH -> Icons.Filled.BatteryChargingFull
                                ComplicationType.BATTERY_PHONE -> Icons.Filled.Smartphone
                                ComplicationType.WEATHER_TEMP -> Icons.Filled.WbSunny
                                ComplicationType.CALORIES -> Icons.Filled.LocalFireDepartment
                                ComplicationType.DATE_BADGE -> Icons.Filled.CalendarMonth
                                ComplicationType.UV_INDEX -> Icons.Filled.LightMode
                                ComplicationType.SLEEP_SCORE -> Icons.Filled.Bedtime
                                ComplicationType.STRESS_LEVEL -> Icons.Filled.Psychology
                                ComplicationType.DISTANCE -> Icons.Filled.Straighten
                                ComplicationType.SUNRISE_SUNSET -> Icons.Filled.WbTwilight
                                ComplicationType.NEXT_EVENT -> Icons.Filled.Event
                                ComplicationType.WORLD_CLOCK -> Icons.Filled.Public
                                ComplicationType.MOON_PHASE -> Icons.Filled.NightlightRound
                                ComplicationType.BAROMETER -> Icons.Filled.Compress
                                else -> Icons.Filled.Add
                            },
                            contentDescription = null,
                            tint = GalaxyCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = compType.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Cambiar",
                        tint = GalaxyCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SIDE-PANEL SECTION 4: DIAL & BEZEL CUSTOMIZER
// -------------------------------------------------------------

@Composable
fun DialAndBezelCustomizer(
    watchFace: WatchFaceEntity,
    onUpdate: ((WatchFaceEntity) -> WatchFaceEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("section_dial_bezel_customizer"),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Dial Type Preset
        Text(
            text = "Tipo de Esfera",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        WatchDialType.values().forEach { dial ->
            val isSelected = watchFace.dialType == dial
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onUpdate { it.copy(dialType = dial) } },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) GalaxyCyan.copy(alpha = 0.12f) else DarkSurfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) GalaxyCyan else DarkBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = dial.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) GalaxyCyan else TextPrimary
                        )
                        Text(
                            text = "Categoría: ${dial.category}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                    RadioButton(
                        selected = isSelected,
                        onClick = { onUpdate { it.copy(dialType = dial) } },
                        colors = RadioButtonDefaults.colors(selectedColor = GalaxyCyan)
                    )
                }
            }
        }

        // 2. Bezel Ring Style
        Text(
            text = "Anillo de Bisel Exterior",
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
                    .clickable { onUpdate { it.copy(bezelStyle = bezel) } },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) GalaxyCyan.copy(alpha = 0.12f) else DarkSurfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) GalaxyCyan else DarkBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = bezel.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) GalaxyCyan else TextPrimary
                    )
                    RadioButton(
                        selected = isSelected,
                        onClick = { onUpdate { it.copy(bezelStyle = bezel) } },
                        colors = RadioButtonDefaults.colors(selectedColor = GalaxyCyan)
                    )
                }
            }
        }

        // 3. Hour Markers
        Text(
            text = "Marcadores de Horas",
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
                    .clickable { onUpdate { it.copy(hourMarkerStyle = marker) } },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) GalaxyCyan.copy(alpha = 0.12f) else DarkSurfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) GalaxyCyan else DarkBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = marker.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) GalaxyCyan else TextPrimary
                    )
                    RadioButton(
                        selected = isSelected,
                        onClick = { onUpdate { it.copy(hourMarkerStyle = marker) } },
                        colors = RadioButtonDefaults.colors(selectedColor = GalaxyCyan)
                    )
                }
            }
        }
    }
}
