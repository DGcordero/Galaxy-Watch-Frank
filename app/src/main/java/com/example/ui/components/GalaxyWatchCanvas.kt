package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.model.*
import java.util.Calendar
import kotlin.math.*

enum class WatchViewMode {
    ACTIVE,
    ALWAYS_ON_DISPLAY,
    NIGHT_RED_SHIFT
}

@Composable
fun GalaxyWatchCanvas(
    watchFace: WatchFaceEntity,
    healthData: GalaxyHealthSnapshot = remember { GalaxyHealthSnapshot() },
    viewMode: WatchViewMode = WatchViewMode.ACTIVE,
    modifier: Modifier = Modifier,
    onComplicationClick: ((ComplicationSlot, ComplicationType) -> Unit)? = null
) {
    // Current time ticking state
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(watchFace.secondHandMovement, viewMode) {
        if (viewMode == WatchViewMode.ALWAYS_ON_DISPLAY) {
            // AOD updates once a minute
            while (true) {
                currentTimeMillis = System.currentTimeMillis()
                kotlinx.coroutines.delay(10000)
            }
        } else {
            val isSweep = watchFace.secondHandMovement == SecondHandMovement.SWEEP_60FPS
            while (true) {
                currentTimeMillis = System.currentTimeMillis()
                kotlinx.coroutines.delay(if (isSweep) 16L else 1000L)
            }
        }
    }

    // Heartbeat pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartPulse"
    )

    BoxWithConstraints(modifier = modifier.aspectRatio(1f)) {
        val sizePx = constraints.maxWidth.toFloat()

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(watchFace) {
                    detectTapGestures { offset ->
                        if (onComplicationClick == null) return@detectTapGestures
                        val center = Offset(sizePx / 2f, sizePx / 2f)
                        val radius = sizePx / 2f
                        
                        // Detect quadrant/slot of tap
                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        val distFromCenter = sqrt(dx * dx + dy * dy)
                        
                        if (distFromCenter < radius * 0.3f) {
                            val comp = ComplicationType.valueOf(watchFace.complicationCenter)
                            if (comp != ComplicationType.NONE) {
                                onComplicationClick(ComplicationSlot.CENTER, comp)
                            }
                        } else if (distFromCenter < radius * 0.95f) {
                            if (abs(dx) > abs(dy)) {
                                if (dx > 0) {
                                    val comp = ComplicationType.valueOf(watchFace.complicationRight)
                                    onComplicationClick(ComplicationSlot.RIGHT, comp)
                                } else {
                                    val comp = ComplicationType.valueOf(watchFace.complicationLeft)
                                    onComplicationClick(ComplicationSlot.LEFT, comp)
                                }
                            } else {
                                if (dy < 0) {
                                    val comp = ComplicationType.valueOf(watchFace.complicationTop)
                                    onComplicationClick(ComplicationSlot.TOP, comp)
                                } else {
                                    val comp = ComplicationType.valueOf(watchFace.complicationBottom)
                                    onComplicationClick(ComplicationSlot.BOTTOM, comp)
                                }
                            }
                        }
                    }
                }
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.width / 2f - 4.dp.toPx()

            val calendar = Calendar.getInstance().apply { timeInMillis = currentTimeMillis }
            val hour = calendar.get(Calendar.HOUR)
            val minute = calendar.get(Calendar.MINUTE)
            val second = calendar.get(Calendar.SECOND)
            val millis = calendar.get(Calendar.MILLISECOND)

            val smoothSecond = if (watchFace.secondHandMovement == SecondHandMovement.SWEEP_60FPS) {
                second + (millis / 1000f)
            } else {
                second.toFloat()
            }
            val smoothMinute = minute + (smoothSecond / 60f)
            val smoothHour = (hour % 12) + (smoothMinute / 60f)

            // Resolve effective colors based on viewMode
            val effectiveColors = when (viewMode) {
                WatchViewMode.NIGHT_RED_SHIFT -> WatchColorTheme(
                    primary = Color(0xFFFF2A2A),
                    accent = Color(0xFFFF5555),
                    background = Color(0xFF0F0202),
                    hands = Color(0xFFFF4444),
                    secondHand = Color(0xFFFF2222),
                    subdial = Color(0xFF220505),
                    glow = Color(0xFFFF1111)
                )
                WatchViewMode.ALWAYS_ON_DISPLAY -> WatchColorTheme(
                    primary = Color(watchFace.primaryColor).copy(alpha = 0.85f),
                    accent = Color(watchFace.accentColor).copy(alpha = 0.8f),
                    background = Color.Black,
                    hands = Color.White,
                    secondHand = Color.Transparent,
                    subdial = Color(0xFF111111),
                    glow = Color.Transparent
                )
                WatchViewMode.ACTIVE -> WatchColorTheme(
                    primary = Color(watchFace.primaryColor),
                    accent = Color(watchFace.accentColor),
                    background = Color(watchFace.dialBackgroundColor),
                    hands = Color(watchFace.handsColor),
                    secondHand = Color(watchFace.secondHandColor),
                    subdial = Color(watchFace.subdialColor),
                    glow = Color(watchFace.glowColor)
                )
            }

            // 1. Draw Watch Case & Dial Background
            drawDialBackground(center, radius, watchFace.backgroundPattern, effectiveColors, viewMode)

            // 2. Draw Outer Bezel Ring & Indicators
            drawBezel(center, radius, watchFace.bezelStyle, effectiveColors, viewMode)

            // 3. Draw Hour Markers / Dial Scale
            drawHourMarkers(center, radius, watchFace.hourMarkerStyle, watchFace.fontFamily, effectiveColors, viewMode)

            // 4. Draw Complication Widgets & Health Metrics
            drawComplications(
                center = center,
                radius = radius,
                watchFace = watchFace,
                healthData = healthData,
                colors = effectiveColors,
                viewMode = viewMode,
                pulseScale = pulseScale,
                calendar = calendar
            )

            // 5. Draw Digital Display (if Digital / Cyber / Retro style)
            if (watchFace.dialType == WatchDialType.DIGITAL_CYBER ||
                watchFace.dialType == WatchDialType.DIGITAL_RETRO ||
                watchFace.dialType == WatchDialType.HYBRID_ULTRA
            ) {
                drawDigitalClock(
                    center = center,
                    radius = radius,
                    hour = calendar.get(Calendar.HOUR_OF_DAY),
                    minute = minute,
                    second = second,
                    watchFace = watchFace,
                    colors = effectiveColors,
                    viewMode = viewMode
                )
            }

            // 6. Draw Analog Clock Hands (unless in retro pure digital)
            if (watchFace.dialType != WatchDialType.DIGITAL_RETRO) {
                drawClockHands(
                    center = center,
                    radius = radius,
                    hourAngle = smoothHour * 30f,
                    minuteAngle = smoothMinute * 6f,
                    secondAngle = smoothSecond * 6f,
                    watchFace = watchFace,
                    colors = effectiveColors,
                    viewMode = viewMode
                )
            }

            // 7. Outer Glass Bezel Reflection / Metallic Trim Ring
            drawCircle(
                color = if (viewMode == WatchViewMode.NIGHT_RED_SHIFT) Color(0x33FF0000) else Color(0x22FFFFFF),
                radius = radius + 2.dp.toPx(),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

private data class WatchColorTheme(
    val primary: Color,
    val accent: Color,
    val background: Color,
    val hands: Color,
    val secondHand: Color,
    val subdial: Color,
    val glow: Color
)

private fun DrawScope.drawDialBackground(
    center: Offset,
    radius: Float,
    pattern: WatchBackgroundPattern,
    colors: WatchColorTheme,
    viewMode: WatchViewMode
) {
    if (viewMode == WatchViewMode.ALWAYS_ON_DISPLAY) {
        // True black for AMOLED burn-in protection
        drawCircle(color = Color.Black, radius = radius, center = center)
        return
    }

    when (pattern) {
        WatchBackgroundPattern.AMOLED_BLACK -> {
            drawCircle(color = Color(0xFF04060A), radius = radius, center = center)
        }
        WatchBackgroundPattern.TITANIUM_BRUSHED -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF1E283A), Color(0xFF0F1522), Color(0xFF080B12)),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
            // Titanium radial micro-stripes
            for (i in 0 until 36) {
                val angle = i * 10f * (PI / 180f).toFloat()
                val start = center + Offset(cos(angle) * radius * 0.2f, sin(angle) * radius * 0.2f)
                val end = center + Offset(cos(angle) * radius * 0.95f, sin(angle) * radius * 0.95f)
                drawLine(
                    color = Color.White.copy(alpha = 0.03f),
                    start = start,
                    end = end,
                    strokeWidth = 1f
                )
            }
        }
        WatchBackgroundPattern.CARBON_FIBER -> {
            drawCircle(color = Color(0xFF0D0F14), radius = radius, center = center)
            // Carbon weave texture
            val step = 12f
            val startX = center.x - radius
            val endX = center.x + radius
            val startY = center.y - radius
            val endY = center.y + radius
            
            var y = startY
            while (y <= endY) {
                var x = startX
                while (x <= endX) {
                    val dist = sqrt((x - center.x) * (x - center.x) + (y - center.y) * (y - center.y))
                    if (dist < radius - 8f) {
                        drawRect(
                            color = Color(0xFF171A24),
                            topLeft = Offset(x, y),
                            size = Size(step * 0.8f, step * 0.4f)
                        )
                    }
                    x += step
                }
                y += step
            }
        }
        WatchBackgroundPattern.RADIAL_SUNBURST -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(colors.primary.copy(alpha = 0.4f), colors.background, Color(0xFF030508)),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }
        WatchBackgroundPattern.CYBER_GRID -> {
            drawCircle(color = Color(0xFF030810), radius = radius, center = center)
            // Concentric cyber rings & crosshairs
            drawCircle(
                color = colors.primary.copy(alpha = 0.15f),
                radius = radius * 0.65f,
                center = center,
                style = Stroke(width = 1.5f)
            )
            drawCircle(
                color = colors.primary.copy(alpha = 0.2f),
                radius = radius * 0.35f,
                center = center,
                style = Stroke(width = 1.5f)
            )
        }
        WatchBackgroundPattern.CONSTELLATION_NIGHT -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF101935), Color(0xFF080D1A), Color(0xFF020408)),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
            // Tiny star dots
            val stars = listOf(
                Offset(0.3f, 0.4f), Offset(0.7f, 0.3f), Offset(0.4f, 0.7f),
                Offset(0.6f, 0.65f), Offset(0.25f, 0.55f), Offset(0.75f, 0.5f)
            )
            stars.forEach { s ->
                val pos = Offset(center.x + (s.x - 0.5f) * radius * 1.5f, center.y + (s.y - 0.5f) * radius * 1.5f)
                drawCircle(color = Color.White.copy(alpha = 0.7f), radius = 2f, center = pos)
            }
        }
        WatchBackgroundPattern.DEEP_OCEAN_GRADIENT -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF005C8A), Color(0xFF002744), Color(0xFF020D1A)),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }
        WatchBackgroundPattern.EMERALD_SPORTS_MESH -> {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF00381C), Color(0xFF001F0F), Color(0xFF020A05)),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }
    }
}

private fun DrawScope.drawBezel(
    center: Offset,
    radius: Float,
    bezelStyle: BezelStyle,
    colors: WatchColorTheme,
    viewMode: WatchViewMode
) {
    if (bezelStyle == BezelStyle.NONE) return

    val bezelWidth = radius * 0.12f
    val outerRadius = radius
    val innerRadius = radius - bezelWidth

    // Bezel track
    drawCircle(
        color = if (viewMode == WatchViewMode.ALWAYS_ON_DISPLAY) Color.Black else colors.subdial,
        radius = outerRadius - (bezelWidth / 2f),
        center = center,
        style = Stroke(width = bezelWidth)
    )

    when (bezelStyle) {
        BezelStyle.TACHYMETER -> {
            // Tachymeter markers (400, 300, 240, 180, 120, 90, 60)
            for (i in 0 until 60) {
                val angle = (i * 6f - 90f) * (PI / 180f).toFloat()
                val isMajor = i % 5 == 0
                val p1 = center + Offset(cos(angle) * (outerRadius - 2f), sin(angle) * (outerRadius - 2f))
                val p2 = center + Offset(cos(angle) * (innerRadius + (if (isMajor) 4f else 8f)), sin(angle) * (innerRadius + (if (isMajor) 4f else 8f)))
                drawLine(
                    color = if (isMajor) colors.accent else colors.primary.copy(alpha = 0.5f),
                    start = p1,
                    end = p2,
                    strokeWidth = if (isMajor) 2.5f else 1f
                )
            }
        }
        BezelStyle.DIVER_60MIN -> {
            // 0-15 minute highlight arc
            drawArc(
                color = colors.accent,
                startAngle = -90f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(center.x - (outerRadius - bezelWidth / 2), center.y - (outerRadius - bezelWidth / 2)),
                size = Size((outerRadius - bezelWidth / 2) * 2, (outerRadius - bezelWidth / 2) * 2),
                style = Stroke(width = bezelWidth * 0.9f)
            )
            // 12 o'clock luminous triangle
            val topP1 = center + Offset(0f, -outerRadius + 4f)
            val topP2 = center + Offset(-6f, -innerRadius - 4f)
            val topP3 = center + Offset(6f, -innerRadius - 4f)
            val path = Path().apply {
                moveTo(topP1.x, topP1.y)
                lineTo(topP2.x, topP2.y)
                lineTo(topP3.x, topP3.y)
                close()
            }
            drawPath(path, color = colors.primary)
        }
        BezelStyle.COMPASS_ROSE -> {
            val directions = listOf("N" to -90f, "E" to 0f, "S" to 90f, "W" to 180f)
            directions.forEach { (dir, ang) ->
                val rad = ang * (PI / 180f).toFloat()
                val p1 = center + Offset(cos(rad) * (outerRadius - 2f), sin(rad) * (outerRadius - 2f))
                val p2 = center + Offset(cos(rad) * (innerRadius + 2f), sin(rad) * (innerRadius + 2f))
                drawLine(
                    color = if (dir == "N") colors.accent else colors.primary,
                    start = p1,
                    end = p2,
                    strokeWidth = 3f
                )
            }
        }
        BezelStyle.INNER_SECONDS_TRACK -> {
            for (i in 0 until 60) {
                val angle = (i * 6f - 90f) * (PI / 180f).toFloat()
                val isMajor = i % 5 == 0
                val p1 = center + Offset(cos(angle) * (outerRadius - 2f), sin(angle) * (outerRadius - 2f))
                val p2 = center + Offset(cos(angle) * (outerRadius - (if (isMajor) 12f else 6f)), sin(angle) * (outerRadius - (if (isMajor) 12f else 6f)))
                drawLine(
                    color = if (isMajor) colors.primary else colors.primary.copy(alpha = 0.35f),
                    start = p1,
                    end = p2,
                    strokeWidth = if (isMajor) 2f else 1f
                )
            }
        }
        BezelStyle.MINIMAL_RING, BezelStyle.WORLD_TIME_CITIES -> {
            drawCircle(
                color = colors.primary.copy(alpha = 0.4f),
                radius = outerRadius - 2f,
                center = center,
                style = Stroke(width = 1.5f)
            )
        }
        BezelStyle.NONE -> {}
    }
}

private fun DrawScope.drawHourMarkers(
    center: Offset,
    radius: Float,
    markerStyle: HourMarkerStyle,
    fontFamily: WatchFontFamily,
    colors: WatchColorTheme,
    viewMode: WatchViewMode
) {
    if (markerStyle == HourMarkerStyle.NONE) return

    val markerRadius = radius * 0.78f

    when (markerStyle) {
        HourMarkerStyle.BOLD_INDEX_BARS -> {
            for (i in 0 until 12) {
                val angle = (i * 30f - 90f) * (PI / 180f).toFloat()
                val isQuarter = i % 3 == 0
                val p1 = center + Offset(cos(angle) * markerRadius, sin(angle) * markerRadius)
                val p2 = center + Offset(cos(angle) * (markerRadius - (if (isQuarter) 16f else 10f)), sin(angle) * (markerRadius - (if (isQuarter) 16f else 10f)))
                drawLine(
                    color = if (isQuarter) colors.accent else colors.hands,
                    start = p1,
                    end = p2,
                    strokeWidth = if (isQuarter) 4.5f else 2.5f,
                    cap = StrokeCap.Round
                )
            }
        }
        HourMarkerStyle.MINIMAL_DOTS -> {
            for (i in 0 until 12) {
                val angle = (i * 30f - 90f) * (PI / 180f).toFloat()
                val isQuarter = i % 3 == 0
                val pos = center + Offset(cos(angle) * markerRadius, sin(angle) * markerRadius)
                drawCircle(
                    color = if (isQuarter) colors.accent else colors.primary.copy(alpha = 0.6f),
                    radius = if (isQuarter) 4f else 2.5f,
                    center = pos
                )
            }
        }
        HourMarkerStyle.DIVER_GEOMETRIC -> {
            for (i in 0 until 12) {
                val angle = (i * 30f - 90f) * (PI / 180f).toFloat()
                val pos = center + Offset(cos(angle) * markerRadius, sin(angle) * markerRadius)
                if (i == 0) { // 12 o'clock inverted triangle
                    val p1 = pos + Offset(0f, 6f)
                    val p2 = pos + Offset(-6f, -6f)
                    val p3 = pos + Offset(6f, -6f)
                    val path = Path().apply {
                        moveTo(p1.x, p1.y)
                        lineTo(p2.x, p2.y)
                        lineTo(p3.x, p3.y)
                        close()
                    }
                    drawPath(path, color = colors.accent)
                } else if (i % 3 == 0) { // 3, 6, 9 rectangles
                    drawRect(
                        color = colors.primary,
                        topLeft = Offset(pos.x - 3.5f, pos.y - 8f),
                        size = Size(7f, 16f)
                    )
                } else { // round dots
                    drawCircle(color = colors.hands, radius = 4f, center = pos)
                    drawCircle(color = colors.primary, radius = 5f, center = pos, style = Stroke(width = 1.2f))
                }
            }
        }
        HourMarkerStyle.PILOT_3_6_9_12, HourMarkerStyle.NUMBERS_1_12, HourMarkerStyle.ROMAN_NUMERALS -> {
            // Render text numerals using Android Native Paint for crisp typography
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    color = colors.hands.toArgb()
                    textAlign = Paint.Align.CENTER
                    textSize = radius * 0.13f
                    isAntiAlias = true
                    typeface = when (fontFamily) {
                        WatchFontFamily.ORBITRON, WatchFontFamily.CYBER_LED -> Typeface.MONOSPACE
                        WatchFontFamily.PLAYFAIR_SERIF -> Typeface.SERIF
                        else -> Typeface.DEFAULT_BOLD
                    }
                }

                val labels = when (markerStyle) {
                    HourMarkerStyle.PILOT_3_6_9_12 -> mapOf(0 to "12", 3 to "3", 6 to "6", 9 to "9")
                    HourMarkerStyle.ROMAN_NUMERALS -> mapOf(
                        0 to "XII", 1 to "I", 2 to "II", 3 to "III", 4 to "IV", 5 to "V",
                        6 to "VI", 7 to "VII", 8 to "VIII", 9 to "IX", 10 to "X", 11 to "XI"
                    )
                    else -> (0 until 12).associateWith { if (it == 0) "12" else it.toString() }
                }

                for ((idx, text) in labels) {
                    val angle = (idx * 30f - 90f) * (PI / 180f).toFloat()
                    val x = center.x + cos(angle) * (markerRadius - 4f)
                    val y = center.y + sin(angle) * (markerRadius - 4f) + (paint.textSize / 3f)
                    
                    if (idx % 3 == 0 && markerStyle == HourMarkerStyle.PILOT_3_6_9_12) {
                        paint.color = colors.accent.toArgb()
                    } else {
                        paint.color = colors.hands.toArgb()
                    }
                    canvas.nativeCanvas.drawText(text, x, y, paint)
                }

                // If pilot style, also draw intermediate index bars for non-quarter hours
                if (markerStyle == HourMarkerStyle.PILOT_3_6_9_12) {
                    for (i in 0 until 12) {
                        if (i % 3 != 0) {
                            val angle = (i * 30f - 90f) * (PI / 180f).toFloat()
                            val p1 = center + Offset(cos(angle) * markerRadius, sin(angle) * markerRadius)
                            val p2 = center + Offset(cos(angle) * (markerRadius - 10f), sin(angle) * (markerRadius - 10f))
                            drawLine(
                                color = colors.primary.copy(alpha = 0.7f),
                                start = p1,
                                end = p2,
                                strokeWidth = 2.5f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }
        HourMarkerStyle.NONE -> {}
    }
}

private fun DrawScope.drawComplications(
    center: Offset,
    radius: Float,
    watchFace: WatchFaceEntity,
    healthData: GalaxyHealthSnapshot,
    colors: WatchColorTheme,
    viewMode: WatchViewMode,
    pulseScale: Float,
    calendar: Calendar
) {
    val compRadius = radius * 0.22f
    val offsetDistance = radius * 0.48f

    val slots = listOf(
        ComplicationSlot.TOP to Offset(center.x, center.y - offsetDistance),
        ComplicationSlot.BOTTOM to Offset(center.x, center.y + offsetDistance),
        ComplicationSlot.LEFT to Offset(center.x - offsetDistance, center.y),
        ComplicationSlot.RIGHT to Offset(center.x + offsetDistance, center.y),
        ComplicationSlot.CENTER to Offset(center.x, center.y + (radius * 0.15f))
    )

    slots.forEach { (slot, pos) ->
        val compTypeName = when (slot) {
            ComplicationSlot.TOP -> watchFace.complicationTop
            ComplicationSlot.BOTTOM -> watchFace.complicationBottom
            ComplicationSlot.LEFT -> watchFace.complicationLeft
            ComplicationSlot.RIGHT -> watchFace.complicationRight
            ComplicationSlot.CENTER -> watchFace.complicationCenter
        }

        val compType = try {
            ComplicationType.valueOf(compTypeName)
        } catch (e: Exception) {
            ComplicationType.NONE
        }

        if (compType != ComplicationType.NONE) {
            drawSingleComplication(
                slot = slot,
                pos = pos,
                radius = compRadius,
                type = compType,
                healthData = healthData,
                colors = colors,
                viewMode = viewMode,
                pulseScale = pulseScale,
                calendar = calendar
            )
        }
    }
}

private fun DrawScope.drawSingleComplication(
    slot: ComplicationSlot,
    pos: Offset,
    radius: Float,
    type: ComplicationType,
    healthData: GalaxyHealthSnapshot,
    colors: WatchColorTheme,
    viewMode: WatchViewMode,
    pulseScale: Float,
    calendar: Calendar
) {
    if (viewMode != WatchViewMode.ALWAYS_ON_DISPLAY) {
        // Sub-dial background bubble
        drawCircle(
            color = colors.subdial.copy(alpha = 0.85f),
            radius = radius,
            center = pos
        )
        drawCircle(
            color = colors.primary.copy(alpha = 0.25f),
            radius = radius,
            center = pos,
            style = Stroke(width = 1f)
        )
    }

    val (valueStr, progress, unitStr, accentColor) = when (type) {
        ComplicationType.HEART_RATE -> Quadruple("${healthData.heartRateBpm}", (healthData.heartRateBpm - 40f) / 140f, "BPM", Color(0xFFFF2A55))
        ComplicationType.STEPS -> Quadruple("${healthData.dailySteps / 1000}k", healthData.stepProgress, "PASOS", Color(0xFF00E676))
        ComplicationType.CALORIES -> Quadruple("${healthData.activeCalories}", healthData.calorieProgress, "KCAL", Color(0xFFFF7A00))
        ComplicationType.BATTERY_WATCH -> Quadruple("${healthData.watchBatteryLevel}%", healthData.watchBatteryProgress, "G-WATCH", colors.primary)
        ComplicationType.BATTERY_PHONE -> Quadruple("${healthData.phoneBatteryLevel}%", healthData.phoneBatteryProgress, "S25 ULTRA", Color(0xFF2D7DFA))
        ComplicationType.WEATHER_TEMP -> Quadruple("${healthData.temperatureCelsius}°", 0.65f, "SOLEADO", Color(0xFFFFD54F))
        ComplicationType.UV_INDEX -> Quadruple("UV ${healthData.uvIndex}", healthData.uvProgress, "MODERADO", Color(0xFFAB47BC))
        ComplicationType.SLEEP_SCORE -> Quadruple("${healthData.sleepScore}", healthData.sleepProgress, "SUEÑO", Color(0xFF26C6DA))
        ComplicationType.STRESS_LEVEL -> Quadruple("${healthData.stressLevel}", healthData.stressProgress, "ESTRÉS", Color(0xFFFF7043))
        ComplicationType.DISTANCE -> Quadruple("${healthData.distanceKm}k", 0.7f, "DIST", colors.accent)
        ComplicationType.SUNRISE_SUNSET -> Quadruple(healthData.sunsetTime, 0.8f, "OCASO", Color(0xFFFF9800))
        ComplicationType.NEXT_EVENT -> Quadruple(healthData.nextEventTime, 0.5f, "EVENTO", colors.primary)
        ComplicationType.WORLD_CLOCK -> Quadruple(healthData.worldTimeCity, 0.5f, healthData.worldTimeDiff, colors.hands)
        ComplicationType.MOON_PHASE -> Quadruple("CREC", 0.45f, "LUNA", Color(0xFFECEFF1))
        ComplicationType.BAROMETER -> Quadruple("${healthData.barometerHpa}", 0.75f, "hPa", Color(0xFF80CBC4))
        ComplicationType.DATE_BADGE -> {
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH) + 1
            Quadruple("$day/$month", 1f, "FECHA", colors.accent)
        }
        ComplicationType.NONE -> Quadruple("", 0f, "", Color.Transparent)
    }

    // Draw dynamic progress ring around the complication
    if (progress > 0f && viewMode != WatchViewMode.ALWAYS_ON_DISPLAY) {
        val sweepAngle = progress.coerceIn(0f, 1f) * 270f
        // Track background
        drawArc(
            color = colors.primary.copy(alpha = 0.15f),
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(pos.x - radius * 0.82f, pos.y - radius * 0.82f),
            size = Size(radius * 1.64f, radius * 1.64f),
            style = Stroke(width = 3f, cap = StrokeCap.Round)
        )
        // Active filled progress
        drawArc(
            color = if (viewMode == WatchViewMode.NIGHT_RED_SHIFT) colors.primary else accentColor,
            startAngle = 135f,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(pos.x - radius * 0.82f, pos.y - radius * 0.82f),
            size = Size(radius * 1.64f, radius * 1.64f),
            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
        )
    }

    // Pulse effect for Heart Rate
    if (type == ComplicationType.HEART_RATE && viewMode == WatchViewMode.ACTIVE) {
        drawCircle(
            color = accentColor.copy(alpha = 0.35f),
            radius = 5f * pulseScale,
            center = Offset(pos.x, pos.y - (radius * 0.45f))
        )
    }

    // Draw text inside complication
    drawIntoCanvas { canvas ->
        val valuePaint = Paint().apply {
            color = if (viewMode == WatchViewMode.NIGHT_RED_SHIFT) colors.primary.toArgb() else colors.hands.toArgb()
            textAlign = Paint.Align.CENTER
            textSize = radius * 0.52f
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
        }
        val labelPaint = Paint().apply {
            color = if (viewMode == WatchViewMode.NIGHT_RED_SHIFT) colors.primary.copy(alpha = 0.7f).toArgb() else colors.primary.toArgb()
            textAlign = Paint.Align.CENTER
            textSize = radius * 0.28f
            isAntiAlias = true
            typeface = Typeface.DEFAULT_BOLD
        }

        canvas.nativeCanvas.drawText(valueStr, pos.x, pos.y + (valuePaint.textSize * 0.25f), valuePaint)
        if (unitStr.isNotEmpty()) {
            canvas.nativeCanvas.drawText(unitStr, pos.x, pos.y + (radius * 0.7f), labelPaint)
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

private fun DrawScope.drawDigitalClock(
    center: Offset,
    radius: Float,
    hour: Int,
    minute: Int,
    second: Int,
    watchFace: WatchFaceEntity,
    colors: WatchColorTheme,
    viewMode: WatchViewMode
) {
    val timeStr = String.format("%02d:%02d", hour, minute)
    val secStr = String.format("%02d", second)

    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            color = colors.hands.toArgb()
            textAlign = Paint.Align.CENTER
            textSize = radius * 0.32f
            isAntiAlias = true
            typeface = when (watchFace.fontFamily) {
                WatchFontFamily.ORBITRON, WatchFontFamily.CYBER_LED -> Typeface.MONOSPACE
                WatchFontFamily.PLAYFAIR_SERIF -> Typeface.SERIF
                else -> Typeface.DEFAULT_BOLD
            }
        }

        val yPos = if (watchFace.dialType == WatchDialType.HYBRID_ULTRA) {
            center.y - (radius * 0.32f)
        } else {
            center.y + (paint.textSize * 0.3f)
        }

        canvas.nativeCanvas.drawText(timeStr, center.x - (if (watchFace.dialType != WatchDialType.HYBRID_ULTRA) 12f else 0f), yPos, paint)

        if (viewMode == WatchViewMode.ACTIVE && watchFace.dialType != WatchDialType.HYBRID_ULTRA) {
            val secPaint = Paint().apply {
                color = colors.accent.toArgb()
                textAlign = Paint.Align.LEFT
                textSize = radius * 0.16f
                isAntiAlias = true
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.nativeCanvas.drawText(secStr, center.x + (radius * 0.38f), yPos - (radius * 0.05f), secPaint)
        }
    }
}

private fun DrawScope.drawClockHands(
    center: Offset,
    radius: Float,
    hourAngle: Float,
    minuteAngle: Float,
    secondAngle: Float,
    watchFace: WatchFaceEntity,
    colors: WatchColorTheme,
    viewMode: WatchViewMode
) {
    val hourRad = (hourAngle - 90f) * (PI / 180f).toFloat()
    val minRad = (minuteAngle - 90f) * (PI / 180f).toFloat()
    val secRad = (secondAngle - 90f) * (PI / 180f).toFloat()

    val hourLen = radius * 0.52f
    val minLen = radius * 0.78f
    val secLen = radius * 0.86f
    val tailLen = radius * 0.18f

    when (watchFace.handStyle) {
        WatchHandStyle.SPORT_ARROW, WatchHandStyle.DIVER_LUMINOUS -> {
            // Hour Hand
            val hEnd = center + Offset(cos(hourRad) * hourLen, sin(hourRad) * hourLen)
            val hTail = center - Offset(cos(hourRad) * tailLen, sin(hourRad) * tailLen)
            drawLine(
                color = colors.hands,
                start = hTail,
                end = hEnd,
                strokeWidth = 6.5f,
                cap = StrokeCap.Round
            )
            // Luminous insert on hour hand
            val hLumeStart = center + Offset(cos(hourRad) * hourLen * 0.4f, sin(hourRad) * hourLen * 0.4f)
            val hLumeEnd = center + Offset(cos(hourRad) * hourLen * 0.9f, sin(hourRad) * hourLen * 0.9f)
            drawLine(
                color = colors.primary,
                start = hLumeStart,
                end = hLumeEnd,
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )

            // Minute Hand
            val mEnd = center + Offset(cos(minRad) * minLen, sin(minRad) * minLen)
            val mTail = center - Offset(cos(minRad) * tailLen, sin(minRad) * tailLen)
            drawLine(
                color = colors.hands,
                start = mTail,
                end = mEnd,
                strokeWidth = 5.2f,
                cap = StrokeCap.Round
            )
            val mLumeStart = center + Offset(cos(minRad) * minLen * 0.35f, sin(minRad) * minLen * 0.35f)
            val mLumeEnd = center + Offset(cos(minRad) * minLen * 0.92f, sin(minRad) * minLen * 0.92f)
            drawLine(
                color = colors.primary,
                start = mLumeStart,
                end = mLumeEnd,
                strokeWidth = 2.4f,
                cap = StrokeCap.Round
            )
        }
        WatchHandStyle.CLASSIC_SWORD -> {
            // Tapered sword hands
            val hEnd = center + Offset(cos(hourRad) * hourLen, sin(hourRad) * hourLen)
            val mEnd = center + Offset(cos(minRad) * minLen, sin(minRad) * minLen)
            drawLine(color = colors.hands, start = center, end = hEnd, strokeWidth = 5.5f, cap = StrokeCap.Square)
            drawLine(color = colors.hands, start = center, end = mEnd, strokeWidth = 4f, cap = StrokeCap.Square)
        }
        WatchHandStyle.CHRONO_NEEDLE -> {
            // Ultra-slim precision needles
            val hEnd = center + Offset(cos(hourRad) * hourLen, sin(hourRad) * hourLen)
            val mEnd = center + Offset(cos(minRad) * minLen, sin(minRad) * minLen)
            drawLine(color = colors.hands, start = center, end = hEnd, strokeWidth = 3.5f, cap = StrokeCap.Round)
            drawLine(color = colors.hands, start = center, end = mEnd, strokeWidth = 2.5f, cap = StrokeCap.Round)
        }
        WatchHandStyle.SKELETON_LUXURY -> {
            val hEnd = center + Offset(cos(hourRad) * hourLen, sin(hourRad) * hourLen)
            val mEnd = center + Offset(cos(minRad) * minLen, sin(minRad) * minLen)
            drawLine(color = colors.accent, start = center, end = hEnd, strokeWidth = 6f, cap = StrokeCap.Round)
            drawLine(color = Color.Black, start = center + Offset(cos(hourRad) * hourLen * 0.2f, sin(hourRad) * hourLen * 0.2f), end = center + Offset(cos(hourRad) * hourLen * 0.8f, sin(hourRad) * hourLen * 0.8f), strokeWidth = 3f)
            drawLine(color = colors.accent, start = center, end = mEnd, strokeWidth = 5f, cap = StrokeCap.Round)
            drawLine(color = Color.Black, start = center + Offset(cos(minRad) * minLen * 0.2f, sin(minRad) * minLen * 0.2f), end = center + Offset(cos(minRad) * minLen * 0.85f, sin(minRad) * minLen * 0.85f), strokeWidth = 2.5f)
        }
        WatchHandStyle.NEON_BEAM -> {
            val hEnd = center + Offset(cos(hourRad) * hourLen, sin(hourRad) * hourLen)
            val mEnd = center + Offset(cos(minRad) * minLen, sin(minRad) * minLen)
            // Laser glow
            drawLine(color = colors.glow.copy(alpha = 0.4f), start = center, end = hEnd, strokeWidth = 8f, cap = StrokeCap.Round)
            drawLine(color = colors.hands, start = center, end = hEnd, strokeWidth = 3.5f, cap = StrokeCap.Round)
            drawLine(color = colors.glow.copy(alpha = 0.4f), start = center, end = mEnd, strokeWidth = 7f, cap = StrokeCap.Round)
            drawLine(color = colors.hands, start = center, end = mEnd, strokeWidth = 2.8f, cap = StrokeCap.Round)
        }
        WatchHandStyle.MINIMAL_BAR, WatchHandStyle.DIGITAL_SEGMENT_ARCS -> {
            val hEnd = center + Offset(cos(hourRad) * hourLen, sin(hourRad) * hourLen)
            val mEnd = center + Offset(cos(minRad) * minLen, sin(minRad) * minLen)
            drawLine(color = colors.hands, start = center, end = hEnd, strokeWidth = 4.5f, cap = StrokeCap.Round)
            drawLine(color = colors.hands, start = center, end = mEnd, strokeWidth = 3.5f, cap = StrokeCap.Round)
        }
    }

    // Second Hand (Sweeping Ultra Orange with counterbalance tail)
    if (viewMode == WatchViewMode.ACTIVE && watchFace.secondHandMovement != SecondHandMovement.HIDDEN) {
        val sEnd = center + Offset(cos(secRad) * secLen, sin(secRad) * secLen)
        val sTail = center - Offset(cos(secRad) * tailLen, sin(secRad) * tailLen)

        // Needle line
        drawLine(
            color = colors.secondHand,
            start = sTail,
            end = sEnd,
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
        // Second tip dot / beacon
        val tipBeacon = center + Offset(cos(secRad) * secLen * 0.72f, sin(secRad) * secLen * 0.72f)
        drawCircle(color = colors.secondHand, radius = 3.5f, center = tipBeacon)
    }

    // Center Cap / Pivot Pin
    drawCircle(color = colors.secondHand, radius = 5f, center = center)
    drawCircle(color = Color.White, radius = 2f, center = center)
}
