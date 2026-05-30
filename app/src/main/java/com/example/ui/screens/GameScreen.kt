package com.example.ui.screens

import android.text.format.DateUtils
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.*
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onBackToMenu: () -> Unit
) {
    val state by viewModel.gameState.collectAsState()
    val carConfig by viewModel.carConfigState.collectAsState()

    // Vibrant Palette Theme Colors
    val slateBG = Color(0xFF0F172A)
    val slateCard = Color(0xFF1E293B)
    val blueAccent = Color(0xFF3B82F6)
    val yellowGold = Color(0xFFFFD700)
    val greenNeon = Color(0xFF10B981)
    val redDanger = Color(0xFFEF4444)

    // Interactive Inputs Tracking
    val leftInteraction = remember { MutableInteractionSource() }
    val rightInteraction = remember { MutableInteractionSource() }
    val gasInteraction = remember { MutableInteractionSource() }
    val brakeInteraction = remember { MutableInteractionSource() }
    val nitroInteraction = remember { MutableInteractionSource() }

    val leftPressed by leftInteraction.collectIsPressedAsState()
    val rightPressed by rightInteraction.collectIsPressedAsState()
    val gasPressed by gasInteraction.collectIsPressedAsState()
    val brakePressed by brakeInteraction.collectIsPressedAsState()
    val nitroPressed by nitroInteraction.collectIsPressedAsState()

    // Direct input streams mapping
    LaunchedEffect(leftPressed, rightPressed) {
        viewModel.steerInput = if (leftPressed) -1.0 else if (rightPressed) 1.0 else 0.0
    }
    LaunchedEffect(gasPressed) {
        viewModel.accelInput = if (gasPressed) 1.0 else 0.0
    }
    LaunchedEffect(brakePressed) {
        viewModel.brakeInput = if (brakePressed) 1.0 else 0.0
    }
    LaunchedEffect(nitroPressed) {
        viewModel.nitroActive = nitroPressed
    }

    // Auto bullet firing simulation for gang members
    LaunchedEffect(state.status, carConfig.familyLevel) {
        if (state.status == GameStatus.GAMEPLAY && carConfig.familyLevel > 0) {
            while (true) {
                kotlinx.coroutines.delay(800L / carConfig.familyLevel.toLong())
                viewModel.fireActiveWeapon()
            }
        }
    }

    // Alarm blinks helper
    val infiniteTransition = rememberInfiniteTransition(label = "siren_blink")
    val sirenBlinkPhase by infiniteTransition.animateFloat(
        initialValue = 0.0f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sirenBlink"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(slateBG)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // CASE: Render BUSTED modal overlay
        if (state.status == GameStatus.BUSTED) {
            GameResultOverlay(
                title = "ПОЙМАН ДПС!",
                titleColor = redDanger,
                score = state.score,
                cashEarned = state.cashEarned,
                isEscaped = false,
                onBack = onBackToMenu,
                onRestart = { viewModel.initiateGameBoot() }
            )
            return@Box
        }

        // CASE: Render ESCAPED modal overlay
        if (state.status == GameStatus.ESCAPED) {
            GameResultOverlay(
                title = "ОТОРВАЛСЯ (ОЧКИ x2)!",
                titleColor = greenNeon,
                score = state.score,
                cashEarned = state.cashEarned + state.score / 2,
                isEscaped = true,
                onBack = onBackToMenu,
                onRestart = { viewModel.initiateGameBoot() }
            )
            return@Box
        }

        val paintCarColor = carConfig.carColor
        val mapObstacles = viewModel.obstacles

        // Core Gaming simulation viewport
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("game_canvas_viewport")
        ) {
            val viewW = size.width
            val viewH = size.height
            val cx = viewW / 2
            val cy = viewH / 2

            val px = state.playerX.toFloat()
            val py = state.playerY.toFloat()

            // Translate camera viewport relative to player 
            drawContext.canvas.save()
            drawContext.transform.translate(cx - px, cy - py)

            // A. Draw green lawn grass field base based on winter or overcast climate
            val isWinter = carConfig.targetWeather == "SNOW"
            if (isWinter) {
                // Slushy dirty siberian winter ground
                drawRect(
                    color = Color(0xFFD8DBE2),
                    topLeft = Offset(0f, 0f),
                    size = Size(viewModel.mapSize.toFloat(), viewModel.mapSize.toFloat())
                )
                // Spawn dirty melting snow spots
                for (spot in 250..3750 step 500) {
                    drawCircle(
                        color = Color(0xFF8B8880),
                        radius = 25f,
                        center = Offset(spot.toFloat(), (spot + 120) % 3600f)
                    )
                }
            } else {
                // Standard gloomy autumn dark grass
                drawRect(
                    color = Color(0xFF142416),
                    topLeft = Offset(0f, 0f),
                    size = Size(viewModel.mapSize.toFloat(), viewModel.mapSize.toFloat())
                )
            }

            // B. Draw tarmac roads lines
            val rWidth = 190f
            val maxBound = viewModel.mapSize.toFloat()

            for (i in 0..5) {
                val offset = i * 800f + 400f
                val roadColor = if (isWinter) Color(0xFF334155) else Color(0xFF1B1D22)

                drawRect(
                    color = roadColor,
                    topLeft = Offset(offset - rWidth / 2f, 0f),
                    size = Size(rWidth, maxBound)
                )

                drawRect(
                    color = roadColor,
                    topLeft = Offset(0f, offset - rWidth / 2f),
                    size = Size(maxBound, rWidth)
                )

                // Divider dots
                var dashSeg = 0f
                while (dashSeg < maxBound) {
                    drawLine(
                        color = Color(0xFFEAB308),
                        start = Offset(offset, dashSeg),
                        end = Offset(offset, dashSeg + 25f),
                        strokeWidth = 3f
                    )
                    drawLine(
                        color = Color(0xFFEAB308),
                        start = Offset(dashSeg, offset),
                        end = Offset(dashSeg + 25f, offset),
                        strokeWidth = 3f
                    )
                    dashSeg += 60f
                }
            }

            // C. Draw drift lines
            state.driftTrails.forEach { drift ->
                drawCircle(
                    color = Color.Black.copy(alpha = drift.alpha),
                    radius = 4.5f,
                    center = Offset(drift.x, drift.y)
                )
            }

            // D. Draw concrete Khrushchev panel buildings
            mapObstacles.forEach { obs ->
                val pad = 100
                if (obs.rect.right >= px - cx - pad && obs.rect.left <= px + cx + pad &&
                    obs.rect.bottom >= py - cy - pad && obs.rect.top <= py + cy + pad
                ) {
                    when (obs.type) {
                        ObstacleType.BUILDING -> {
                            // High rise panel structure
                            drawRoundRect(
                                color = Color(0xFF475569),
                                topLeft = Offset(obs.rect.left, obs.rect.top),
                                size = Size(obs.rect.width, obs.rect.height),
                                cornerRadius = CornerRadius(10f, 10f)
                            )
                            // Rows of yellow windows
                            val winW = obs.rect.width
                            val winH = obs.rect.height
                            var curX = obs.rect.left + 15f
                            while (curX < obs.rect.right - 15f) {
                                var curY = obs.rect.top + 15f
                                while (curY < obs.rect.bottom - 15f) {
                                    val isLit = (curX.toInt() + curY.toInt()) % 4 == 0
                                    drawRect(
                                        color = if (isLit) Color(0xFFFEF08A) else Color(0xFF334155),
                                        topLeft = Offset(curX, curY),
                                        size = Size(10f, 8f)
                                    )
                                    curY += 24f
                                }
                                curX += 28f
                            }

                            drawRoundRect(
                                color = Color(0xFF1E293B),
                                topLeft = Offset(obs.rect.left, obs.rect.top),
                                size = Size(obs.rect.width, obs.rect.height),
                                cornerRadius = CornerRadius(10f, 10f),
                                style = Stroke(width = 5f)
                            )
                        }
                        ObstacleType.WATER -> {
                            drawRoundRect(
                                color = if (isWinter) Color(0xFF0369A1) else Color(0xFF0C4A6E),
                                topLeft = Offset(obs.rect.left, obs.rect.top),
                                size = Size(obs.rect.width, obs.rect.height),
                                cornerRadius = CornerRadius(16f, 16f)
                            )
                            drawRoundRect(
                                color = Color(0xFF38BDF8),
                                topLeft = Offset(obs.rect.left, obs.rect.top),
                                size = Size(obs.rect.width, obs.rect.height),
                                cornerRadius = CornerRadius(16f, 16f),
                                style = Stroke(width = 4f)
                            )
                        }
                        else -> {}
                    }
                }
            }

            // E. Render pickups (Coins and tools)
            state.items.forEach { item ->
                if (!item.isCollected) {
                    if (item.x >= px - cx - 50 && item.x <= px + cx + 50 &&
                        item.y >= py - cy - 50 && item.y <= py + cy + 50
                    ) {
                        val ix = item.x.toFloat()
                        val iy = item.y.toFloat()

                        when (item.type) {
                            ItemType.COIN -> {
                                drawCircle(
                                    color = Color(0xFF22C55E),
                                    radius = 12f,
                                    center = Offset(ix, iy)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 9f,
                                    center = Offset(ix, iy),
                                    style = Stroke(width = 2.4f)
                                )
                            }
                            ItemType.REPAIR -> {
                                drawRect(
                                    color = Color(0xFFEF4444),
                                    topLeft = Offset(ix - 10f, iy - 10f),
                                    size = Size(20f, 20f)
                                )
                                drawLine(
                                    color = Color.White,
                                    start = Offset(ix - 6f, iy),
                                    end = Offset(ix + 6f, iy),
                                    strokeWidth = 3f
                                )
                                drawLine(
                                    color = Color.White,
                                    start = Offset(ix, iy - 6f),
                                    end = Offset(ix, iy + 6f),
                                    strokeWidth = 3f
                                )
                            }
                            ItemType.NITRO -> {
                                val path = Path().apply {
                                    moveTo(ix, iy - 14f)
                                    lineTo(ix - 7f, iy + 2f)
                                    lineTo(ix + 2f, iy + 2f)
                                    lineTo(ix - 2f, iy + 14f)
                                    lineTo(ix + 9f, iy - 2f)
                                    lineTo(ix + 2f, iy - 2f)
                                    close()
                                }
                                drawPath(path = path, color = Color(0xFF00D2FF))
                            }
                        }
                    }
                }
            }

            // F. Bullet lines tracers
            state.bullets.forEach { bullet ->
                drawLine(
                    color = Color(0xFFF97316),
                    start = Offset(bullet.x.toFloat(), bullet.y.toFloat()),
                    end = Offset((bullet.x - bullet.vx * 0.9f).toFloat(), (bullet.y - bullet.vy * 0.9f).toFloat()),
                    strokeWidth = 4f
                )
            }

            // G. Render Police cars
            state.copCars.forEach { cop ->
                val cxCar = cop.x.toFloat()
                val cyCar = cop.y.toFloat()

                rotate(
                    degrees = Math.toDegrees(cop.angle).toFloat(),
                    pivot = Offset(cxCar, cyCar)
                ) {
                    val copColor = if (cop.isStruck) Color(0xFFFECDD3) else Color(0xFF1E293B)
                    drawRoundRect(
                        color = copColor,
                        topLeft = Offset(cxCar - 22f, cyCar - 12f),
                        size = Size(44f, 24f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    // Cop white trim
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(cxCar - 9f, cyCar - 12f),
                        size = Size(18f, 24f)
                    )
                    // Cop glass windshield
                    drawRect(
                        color = Color(0xFF0F172A),
                        topLeft = Offset(cxCar + 2f, cyCar - 10f),
                        size = Size(4f, 20f)
                    )

                    // Sirens
                    val flashSirenRed = (System.currentTimeMillis() / 150L) % 2 == 0L
                    drawRect(
                        color = if (flashSirenRed) Color.Red else Color.Blue,
                        topLeft = Offset(cxCar - 4f, cyCar - 10f),
                        size = Size(8f, 10f)
                    )
                    drawRect(
                        color = if (flashSirenRed) Color.Blue else Color.Red,
                        topLeft = Offset(cxCar - 4f, cyCar),
                        size = Size(8f, 10f)
                    )
                }
            }

            // H. Render Player Lada Car
            rotate(
                degrees = Math.toDegrees(state.playerAngle).toFloat(),
                pivot = Offset(px, py)
            ) {
                // Neon Underglow (green green glows)
                if (carConfig.neonUnderglow) {
                    drawCircle(
                        color = Color(0xFF22C55E).copy(alpha = 0.35f),
                        radius = 35f,
                        center = Offset(px, py)
                    )
                }

                // Headlight rays
                val conePath = Path().apply {
                    moveTo(px + 20f, py)
                    lineTo(px + 230f, py - 80f)
                    lineTo(px + 230f, py + 80f)
                    close()
                }
                drawPath(
                    path = conePath,
                    color = Color(0xFFFFD700).copy(alpha = 0.14f)
                )

                // Wheels detail lines
                drawRect(Color.Black, topLeft = Offset(px - 16f, py - 18f), size = Size(10f, 5f))
                drawRect(Color.Black, topLeft = Offset(px + 10f, py - 18f), size = Size(10f, 5f))
                drawRect(Color.Black, topLeft = Offset(px - 16f, py + 13f), size = Size(10f, 5f))
                drawRect(Color.Black, topLeft = Offset(px + 10f, py + 13f), size = Size(10f, 5f))

                // Custom database-selected paint
                drawRoundRect(
                    color = Color(paintCarColor),
                    topLeft = Offset(px - 22f, py - 14f),
                    size = Size(44f, 28f),
                    cornerRadius = CornerRadius(6f, 6f)
                )

                // Glass screen interior
                val glassPath = Path().apply {
                    moveTo(px - 8f, py - 10f)
                    lineTo(px + 12f, py - 10f)
                    lineTo(px + 18f, py)
                    lineTo(px + 12f, py + 10f)
                    lineTo(px - 8f, py + 10f)
                    close()
                }
                drawPath(glassPath, Color(0xFF0F172A))

                // Aerodynamic Spoiler block if active
                if (carConfig.bigSpoiler) {
                    drawLine(
                        color = Color.White,
                        start = Offset(px - 25f, py - 16f),
                        end = Offset(px - 25f, py + 16f),
                        strokeWidth = 5f
                    )
                }

                // Flame turbo thrust exhaust
                if (state.playerSpeed > 0.1 && viewModel.nitroActive && state.nitroAmount > 0.0) {
                    val flameLength = 30f + (System.currentTimeMillis() % 40)
                    val flamePath = Path().apply {
                        moveTo(px - 22f, py - 6f)
                        lineTo(px - 22f - flameLength, py)
                        lineTo(px - 22f, py + 6f)
                        close()
                    }
                    drawPath(flamePath, Color(0xFF00D2FF))
                }
            }

            // RESTORE Camera
            drawContext.canvas.restore()

            // --- REAL-TIME GRAPHICAL WEATHER OVERLAYS ---
            if (carConfig.targetWeather == "RAIN") {
                // Streaming raindrops
                val timeSec = System.currentTimeMillis() / 12f
                for (k in 0..50) {
                    val rx = (k * 137 + timeSec * 0.5f) % viewW
                    val ry = (k * 223 + timeSec * 3.5f) % viewH
                    drawLine(
                        color = Color(0x663B82F6),
                        start = Offset(rx, ry),
                        end = Offset(rx - 10f, ry + 20f),
                        strokeWidth = 2f
                    )
                }
            } else if (carConfig.targetWeather == "SNOW") {
                // Swirling snowflakes
                val timeSec = System.currentTimeMillis() / 18f
                for (k in 0..45) {
                    val sx = (k * 197 + timeSec * 0.9f) % viewW
                    val sy = (k * 311 + timeSec * 1.5f) % viewH
                    drawCircle(
                        color = Color(0xCCE2E8F0),
                        radius = 4f + (k % 3),
                        center = Offset(sx, sy)
                    )
                }
            } else {
                // OVERCAST Sun details
                drawCircle(
                    color = Color(0x2BFEF08A),
                    radius = 80f,
                    center = Offset(viewW - 100f, 100f)
                )
            }
        }

        // 3. GAMEPLAY TOP PANEL HUD (Score, Cops sirens, Escapist meter)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back to menu
                Button(
                    onClick = onBackToMenu,
                    colors = ButtonDefaults.buttonColors(containerColor = slateCard),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    pText(text = "КОНЦЕДИРОВАТЬ", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color.White)
                }

                Column(horizontalAlignment = Alignment.End) {
                    pText(
                        text = "КУШ: ${state.score} Очков",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    pText(
                        text = "КАРМАН: ${NumberFormat.getNumberInstance(Locale.US).format(state.cashEarned)} ₽",
                        color = greenNeon,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedVisibility(
                visible = state.copsActive,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val backgroundAlarmColor = redDanger.copy(alpha = 0.25f * sirenBlinkPhase)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(backgroundAlarmColor)
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = sirenBlinkPhase), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Alert",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    pText(
                        text = "ВНИМАНИЕ: РОЗЫСК ДПС УРОВЕНЬ ${state.heatLevel} 💀",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            val noCopsNearby = state.escapeCountdown < 5
            AnimatedVisibility(
                visible = state.copsActive && noCopsNearby,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(slateCard)
                        .border(1.dp, blueAccent, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    pText(
                        text = "СКРЫВАЕШЬСЯ... ПОБЕГ ЧЕРЕЗ ${state.escapeCountdown} сек.",
                        color = blueAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            AnimatedVisibility(
                visible = state.bustedProgress > 0.02f,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xE60F172A))
                        .border(1.dp, redDanger, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    pText(text = "ВАС БЛОКИРУЮТ ПАТРУЛИ!", color = redDanger, fontWeight = FontWeight.Black, fontSize = 11.sp)
                    LinearProgressIndicator(
                        progress = { state.bustedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = redDanger,
                        trackColor = Color(0xFF450A0A)
                    )
                }
            }
        }

        // Radar coordinate locator
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 80.dp, start = 16.dp)
                .size(105.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(slateCard.copy(alpha = 0.8f))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val scale = size.width / viewModel.mapSize.toFloat()

                for (i in 0..5) {
                    val roadOffset = (i * 800f + 400f) * scale
                    drawLine(
                        color = Color(0xFF475569),
                        start = Offset(roadOffset, 0f),
                        end = Offset(roadOffset, size.height),
                        strokeWidth = 2.5f
                    )
                    drawLine(
                        color = Color(0xFF475569),
                        start = Offset(0f, roadOffset),
                        end = Offset(size.width, roadOffset),
                        strokeWidth = 2.5f
                    )
                }

                drawCircle(
                    color = Color(0xFF22C55E),
                    radius = 4f,
                    center = Offset(state.playerX.toFloat() * scale, state.playerY.toFloat() * scale)
                )

                state.copCars.forEach { cop ->
                    drawCircle(
                        color = redDanger,
                        radius = 3.5f,
                        center = Offset(cop.x.toFloat() * scale, cop.y.toFloat() * scale)
                    )
                }
            }
        }

        // 4. WEAPONS AND TEAM HUD FLOATING PANEL
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            val currentWeaponName = when (carConfig.weaponLevel) {
                0 -> "Кулаки / Бита"
                1 -> "ПМ Пистолет"
                2 -> "АК-74у Калаш"
                else -> "РПГ-7 Рокета"
            }
            val familyMembersName = when (carConfig.familyLevel) {
                0 -> "В Бегах Один"
                1 -> "Мелкая Братва"
                2 -> "Своя ОПГ"
                else -> "Хозяин Района"
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = slateCard.copy(alpha = 0.9f)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                    .width(130.dp)
                    .height(110.dp)
            ) {
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    Column {
                        pText(text = "🔫 ОРУЖИЕ:", color = redDanger, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        pText(
                            text = currentWeaponName,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    HorizontalDivider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 2.dp))
                    Column {
                        pText(text = "👥 БРАТВА:", color = greenNeon, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        pText(
                            text = familyMembersName,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 5. STATS HUD FOOTER: SPEEDOMETER, NITRO, DURABILITY
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 120.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xCC1E293B))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                pText(
                    text = "СПИДОМЕТР",
                    fontSize = 9.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val displaySpeed = (state.playerSpeed * 15.0).coerceAtLeast(0.0).toInt()
                    pText(
                        text = displaySpeed.toString(),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                    pText(
                        text = "КМ/Ч",
                        fontSize = 11.sp,
                        color = redDanger,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Column(
                modifier = Modifier
                    .width(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xCC1E293B))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        pText(text = "ЦЕЛОСТНОСТЬ ВАЗА", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        pText(
                            text = "${state.playerHealth.toInt()}%",
                            fontSize = 8.sp,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    val healthBarColor = when {
                        state.playerHealth > 50.0 -> greenNeon
                        state.playerHealth > 25.0 -> yellowGold
                        else -> redDanger
                    }
                    LinearProgressIndicator(
                        progress = { (state.playerHealth / 100.0).toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = healthBarColor,
                        trackColor = Color(0xFF0F172A)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    val nitroMaxVal = 100.0 + (carConfig.nitroLevel - 1) * 20.0
                    val nitroPct = (state.nitroAmount / nitroMaxVal).toFloat().coerceIn(0f, 1f)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        pText(text = "ЗАКИСЬ АЗОТА (NITRO)", fontSize = 8.sp, color = Color(0xFF00D2FF), fontWeight = FontWeight.Bold)
                        pText(
                            text = "${(nitroPct * 100).toInt()}%",
                            fontSize = 8.sp,
                            color = Color(0xFF00D2FF),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { nitroPct },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color(0xFF00D2FF),
                        trackColor = Color(0xFF0C191D)
                    )
                }
            }
        }

        // TACTICAL WEAPON FIRING OVERLAY
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 90.dp)
                .padding(horizontal = 24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            if (carConfig.weaponLevel > 0) {
                val gunBadge = when (carConfig.weaponLevel) {
                    1 -> "🔫 ПМ Пистолет"
                    2 -> "⚔️ AK-74у Калаш"
                    else -> "🚀 РПГ-7 Рокета"
                }
                Button(
                    onClick = { viewModel.fireActiveWeapon() },
                    colors = ButtonDefaults.buttonColors(containerColor = redDanger),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .width(190.dp)
                        .height(48.dp)
                        .border(1.dp, Color.White, RoundedCornerShape(14.dp))
                ) {
                    pText(
                        text = "ОГОНЬ! $gunBadge",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(slateCard.copy(alpha = 0.8f))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    pText(
                        text = "⚠️ НЕТ ОРУЖИЯ (КУПИТЕ В ДОНАТ МАГАЗИНЕ)",
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Steering & Throttle pads
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(if (leftPressed) redDanger else slateCard)
                        .border(2.dp, Color(0xFF475569), CircleShape)
                        .clickable(interactionSource = leftInteraction, indication = null) {},
                    contentAlignment = Alignment.Center
                ) {
                    pText(text = "◀", fontSize = 24.sp, color = Color.White)
                }

                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(if (rightPressed) redDanger else slateCard)
                        .border(2.dp, Color(0xFF475569), CircleShape)
                        .clickable(interactionSource = rightInteraction, indication = null) {},
                    contentAlignment = Alignment.Center
                ) {
                    pText(text = "▶", fontSize = 24.sp, color = Color.White)
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(if (brakePressed) yellowGold else slateCard)
                        .border(1.dp, Color(0xFF475569), CircleShape)
                        .clickable(interactionSource = brakeInteraction, indication = null) {},
                    contentAlignment = Alignment.Center
                ) {
                    pText(text = "STOP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (brakePressed) Color.Black else Color.White)
                }

                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(if (nitroPressed) Color(0xFF00D2FF) else slateCard)
                        .border(1.dp, Color(0xFF475569), CircleShape)
                        .clickable(interactionSource = nitroInteraction, indication = null) {},
                    contentAlignment = Alignment.Center
                ) {
                    pText(text = "NITRO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (nitroPressed) Color.Black else Color.White)
                }

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(if (gasPressed) greenNeon else slateCard)
                        .border(2.dp, Color(0xFF475569), CircleShape)
                        .clickable(interactionSource = gasInteraction, indication = null) {},
                    contentAlignment = Alignment.Center
                ) {
                    pText(text = "ГАЗ", fontSize = 16.sp, fontWeight = FontWeight.Black, color = if (gasPressed) Color.Black else Color.White)
                }
            }
        }
    }
}

@Composable
fun GameResultOverlay(
    title: String,
    titleColor: Color,
    score: Int,
    cashEarned: Int,
    isEscaped: Boolean,
    onBack: () -> Unit,
    onRestart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xE60F172A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E293B))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = titleColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                pText(text = "ПОЛУЧЕНО ОЧКОВ:", color = Color(0xFF94A3B8), fontSize = 14.sp)
                pText(
                    text = score.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                pText(text = "ВЫПЛАТЫ В ГАРАЖ:", color = Color(0xFF94A3B8), fontSize = 14.sp)
                pText(
                    text = "+₽$cashEarned",
                    color = Color(0xFF22C55E),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                pText(text = "ЕЩЕ ОДИН ВЫЕЗД", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
            }

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(1.dp, Color(0xFF475569), RoundedCornerShape(12.dp))
            ) {
                pText(text = "ВЕРНУТЬСЯ В ГАРАЖ", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun pText(
    text: String,
    color: Color = Color.White,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    fontFamily: FontFamily = FontFamily.SansSerif,
    textAlign: TextAlign = TextAlign.Start,
    lineSpacingCheck: androidx.compose.ui.unit.TextUnit = 18.sp,
    letterSpacing: androidx.compose.ui.unit.TextUnit = 0.sp
) {
    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        textAlign = textAlign,
        lineHeight = lineSpacingCheck,
        letterSpacing = letterSpacing,
        modifier = Modifier.padding(1.dp)
    )
}
