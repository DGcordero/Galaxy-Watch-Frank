package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.WatchFaceRepository
import com.example.model.WatchFaceEntity
import com.example.ui.theme.*

@Composable
fun ShareExportDialog(
    watchFace: WatchFaceEntity,
    repository: WatchFaceRepository,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedFormatTab by remember { mutableIntStateOf(0) } // 0: JSON (.gwatchface), 1: WFF (Watch Face Format XML)
    var copiedFeedback by remember { mutableStateOf<String?>(null) }

    val jsonContent = remember(watchFace) { repository.exportToJson(watchFace) }
    val wffXmlContent = remember(watchFace) { repository.generateWatchFaceFormatXml(watchFace) }

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
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Compartir y Exportar",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = watchFace.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = GalaxyCyan
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Format selector tabs
                TabRow(
                    selectedTabIndex = selectedFormatTab,
                    containerColor = DarkSurfaceVariant,
                    contentColor = GalaxyCyan,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedFormatTab]),
                            color = GalaxyCyan
                        )
                    }
                ) {
                    Tab(
                        selected = selectedFormatTab == 0,
                        onClick = { selectedFormatTab = 0 },
                        text = { Text("JSON Galaxy (.gwatchface)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedFormatTab == 1,
                        onClick = { selectedFormatTab = 1 },
                        text = { Text("Wear OS 5 (WFF XML)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Code preview box
                val currentText = if (selectedFormatTab == 0) jsonContent else wffXmlContent

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF070A10))
                        .padding(12.dp)
                ) {
                    Text(
                        text = currentText,
                        color = Color(0xFF64FFDA),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }

                if (copiedFeedback != null) {
                    Text(
                        text = copiedFeedback!!,
                        color = GalaxyEmerald,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Galaxy WatchFace", currentText)
                            clipboard.setPrimaryClip(clip)
                            copiedFeedback = "✓ Código copiado al portapapeles"
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                        )
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copiar", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Esfera para Galaxy Watch: ${watchFace.title}")
                                putExtra(Intent.EXTRA_TEXT, "¡Mira mi esfera personalizada para Samsung Galaxy Watch!\n\n${watchFace.title} por ${watchFace.author}\n\nCódigo de diseño:\n$jsonContent")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Compartir esfera con amigos"))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GalaxyUltraOrange)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Compartir", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
