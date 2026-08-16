package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WatchFaceEntity
import com.example.ui.components.GalaxyWatchCanvas
import com.example.ui.components.WatchViewMode
import com.example.ui.dialogs.ImportWatchFaceDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.WatchStudioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    viewModel: WatchStudioViewModel,
    onNavigateToStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allFaces by viewModel.allWatchFaces.collectAsState()
    val userFaces by viewModel.userCreatedWatchFaces.collectAsState()
    val favoriteFaces by viewModel.favoriteWatchFaces.collectAsState()

    var selectedFilterCategory by remember { mutableStateOf("Todas") }
    var searchQuery by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }

    val categories = listOf("Todas", "Mis Creaciones", "Favoritos", "Híbrido", "Analógico", "Digital", "Deporte", "Lujo", "Minimalista")

    val displayedFaces = remember(allFaces, userFaces, favoriteFaces, selectedFilterCategory, searchQuery) {
        val baseList = when (selectedFilterCategory) {
            "Mis Creaciones" -> userFaces
            "Favoritos" -> favoriteFaces
            "Todas" -> allFaces
            else -> allFaces.filter { it.category.equals(selectedFilterCategory, ignoreCase = true) }
        }
        if (searchQuery.isBlank()) {
            baseList
        } else {
            baseList.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.author.contains(searchQuery, ignoreCase = true) ||
                it.description.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.createNewWatchFace()
                    onNavigateToStudio()
                },
                containerColor = GalaxyUltraOrange,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Diseñar Nueva", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("create_new_watchface_fab")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Galería & Comunidad",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Explora y comparte esferas para Galaxy Watch",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                FilledTonalButton(
                    onClick = { showImportDialog = true },
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = DarkSurfaceVariant)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = GalaxyCyan)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Importar", color = GalaxyCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por estilo, autor o modelo...", color = TextTertiary, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = TextSecondary)
                        }
                    }
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedBorderColor = GalaxyCyan,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Pills
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    val isSelected = selectedFilterCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilterCategory = category },
                        label = { Text(category, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GalaxyCyan.copy(alpha = 0.2f),
                            selectedLabelColor = GalaxyCyan,
                            containerColor = DarkSurface,
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

            Spacer(modifier = Modifier.height(12.dp))

            // Watch Face Cards Grid / List
            if (displayedFaces.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.WatchLater, contentDescription = null, tint = TextTertiary, modifier = Modifier.size(56.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No se encontraron esferas en esta categoría", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(displayedFaces, key = { it.id }) { face ->
                        WatchFaceCardItem(
                            watchFace = face,
                            onEdit = {
                                viewModel.setEditingWatchFace(face)
                                onNavigateToStudio()
                            },
                            onSync = { viewModel.applyAndSyncActiveWatchFace(face) },
                            onDuplicate = { viewModel.duplicateWatchFace(face) },
                            onToggleFavorite = { viewModel.toggleFavorite(face) },
                            onDelete = { viewModel.deleteWatchFace(face) }
                        )
                    }
                }
            }
        }
    }

    if (showImportDialog) {
        ImportWatchFaceDialog(
            onImportJson = { json ->
                viewModel.importFromJson(json)
                onNavigateToStudio()
            },
            onDismiss = { showImportDialog = false }
        )
    }
}

@Composable
private fun WatchFaceCardItem(
    watchFace: WatchFaceEntity,
    onEdit: () -> Unit,
    onSync: () -> Unit,
    onDuplicate: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = if (watchFace.isCurrentActive) CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(GalaxyUltraOrange),
            width = 2.dp
        ) else CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(DarkBorder),
            width = 1.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mini Watch Preview (120dp)
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF070A10))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GalaxyWatchCanvas(
                        watchFace = watchFace,
                        viewMode = WatchViewMode.ACTIVE,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Info Column
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = watchFace.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = GalaxyCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (watchFace.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = if (watchFace.isFavorite) GalaxyNeonPink else TextTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = watchFace.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1
                    )

                    Text(
                        text = "por ${watchFace.author}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = watchFace.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Badges row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = GalaxyAmber, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("${watchFace.rating}", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("${watchFace.downloadCount}", fontSize = 11.sp, color = TextSecondary)
                        }

                        if (watchFace.isCurrentActive) {
                            Text(
                                text = "EN TU RELOJ",
                                style = MaterialTheme.typography.labelSmall,
                                color = GalaxyUltraOrange,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(GalaxyUltraOrange.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = DarkBorder)
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (watchFace.isCustomUserCreated) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                    }
                }

                IconButton(onClick = onDuplicate, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Duplicar", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedButton(
                    onClick = onEdit,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(6.dp))

                Button(
                    onClick = onSync,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (watchFace.isCurrentActive) GalaxyEmerald else GalaxyUltraOrange
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (watchFace.isCurrentActive) "Activo" else "Aplicar",
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
