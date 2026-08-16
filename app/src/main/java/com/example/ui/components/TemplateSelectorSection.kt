package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.WatchFaceTemplateCatalog
import com.example.model.*
import com.example.ui.theme.*

/**
 * TemplateSelectorSection allows users to browse pre-designed professional templates
 * organized by categories (Modernos & Futuristas, Clásicos & Lujo, Metálicos, Deportivos, Digitales)
 * and apply them instantly to the editor canvas.
 */
@Composable
fun TemplateSelectorSection(
    currentWatchFace: WatchFaceEntity,
    healthData: GalaxyHealthSnapshot,
    onApplyTemplate: (WatchFaceEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(TemplateCategory.ALL) }

    val filteredTemplates = remember(selectedCategory) {
        if (selectedCategory == TemplateCategory.ALL) {
            WatchFaceTemplateCatalog.templates
        } else {
            WatchFaceTemplateCatalog.templates.filter { it.category == selectedCategory }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("template_selector_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header info banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            border = BorderStroke(1.dp, GalaxyCyan.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(GalaxyCyan, GalaxyUltraOrange)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Plantillas Profesionales Pre-diseñadas",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Selecciona una base para comenzar a personalizar esferas futuristas, metálicas, clásicas o deportivas.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Category Filter Chips
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Categorías de Diseño",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = GalaxyCyan
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TemplateCategory.values().forEach { category ->
                    val isSelected = selectedCategory == category
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) GalaxyCyan.copy(alpha = 0.2f) else DarkSurfaceVariant,
                        animationSpec = tween(200),
                        label = "catBg"
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) GalaxyCyan else TextSecondary,
                        animationSpec = tween(200),
                        label = "catText"
                    )

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when (category) {
                                    TemplateCategory.ALL -> Icons.Default.GridView
                                    TemplateCategory.MODERN_FUTURISTIC -> Icons.Default.Psychology
                                    TemplateCategory.CLASSIC_LUXURY -> Icons.Default.WorkspacePremium
                                    TemplateCategory.METALLIC -> Icons.Default.Shield
                                    TemplateCategory.SPORTS -> Icons.Default.DirectionsRun
                                    TemplateCategory.DIGITAL -> Icons.Default.Memory
                                },
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
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
                            borderColor = if (isSelected) GalaxyCyan else DarkBorder,
                            selectedBorderColor = GalaxyCyan
                        ),
                        modifier = Modifier.testTag("filter_chip_${category.name.lowercase()}")
                    )
                }
            }
        }

        // Templates List Cards
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            filteredTemplates.forEach { template ->
                TemplateCardItem(
                    template = template,
                    healthData = healthData,
                    onApply = {
                        // Apply template while preserving current id/title if desired or adopting template design
                        val newFace = template.previewEntity.copy(
                            id = currentWatchFace.id,
                            isCustomUserCreated = true
                        )
                        onApplyTemplate(newFace)
                    }
                )
            }
        }
    }
}

@Composable
private fun TemplateCardItem(
    template: WatchFaceTemplate,
    healthData: GalaxyHealthSnapshot,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .testTag("template_card_${template.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        border = BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live Mini-Canvas Preview
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .border(2.dp, Color(template.previewEntity.primaryColor).copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                GalaxyWatchCanvas(
                    watchFace = template.previewEntity,
                    healthData = healthData,
                    viewMode = WatchViewMode.ACTIVE,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Template Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = template.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Category Tag Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(template.previewEntity.primaryColor).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(template.previewEntity.primaryColor).copy(alpha = 0.35f))
                ) {
                    Text(
                        text = template.tag,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(template.previewEntity.primaryColor),
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.5.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    fontSize = 11.5.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Apply Button
                Button(
                    onClick = onApply,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(template.previewEntity.primaryColor)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .align(Alignment.End)
                        .testTag("apply_template_btn_${template.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Aplicar Plantilla",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 11.5.sp
                    )
                }
            }
        }
    }
}
