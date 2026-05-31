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

    var cameraViewMode by remember { mutableStateOf(0) } // 0 = 3D Chase, 1 = 3D Cockpit/Interior, 2 = 2D Topdown
    val is3DView = cameraViewMode < 2

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
            if (is3DView) {
                val viewW = size.width
                val viewH = size.height
                val cx = viewW / 2
                val cy = viewH / 2

                val px = state.playerX
                val py = state.playerY
                val pAngle = state.playerAngle // in radians
                val isWinter = carConfig.targetWeather == "SNOW"

                // 3D camera settings
                val isInterior = cameraViewMode == 1
                val D_behind = if (isInterior) 28.0f else 150.0f
                val H_above = if (isInterior) 33.0f else 75.0f
                val F = viewW * 1.0f // Focal Length
                val horizonY = cy - viewH * 0.16f // 3D Horizon position

                // Projection Helper
                fun projectGround(wx: Double, wy: Double, wz: Double): Offset? {
                    val dx = wx - (px - kotlin.math.cos(pAngle) * D_behind)
                    val dy = wy - (py - kotlin.math.sin(pAngle) * D_behind)
                    val dz = wz - H_above

                    val rZ = dx * kotlin.math.cos(pAngle) + dy * kotlin.math.sin(pAngle)
                    val rX = -dx * kotlin.math.sin(pAngle) + dy * kotlin.math.cos(pAngle)
                    val rY = dz

                    if (rZ < 8.0) return null // behind camera - skip

                    val sx = cx + (rX.toFloat() * F) / rZ.toFloat()
                    val sy = cy - (rY.toFloat() * F) / rZ.toFloat() + (horizonY - cy)
                    return Offset(sx, sy)
                }

                // 1. Draw Gorgeous 3D Sky Plane (vertical gradient representing weather)
                val skyColors = when (carConfig.targetWeather) {
                    "RAIN" -> listOf(Color(0xFF0F172A), Color(0xFF334155))
                    "SNOW" -> listOf(Color(0xFF1E293B), Color(0xFF475569))
                    else -> listOf(Color(0xFF020617), Color(0xFF0F172A)) // "OVERCAST" default dark tech
                }
                drawRect(
                    brush = Brush.verticalGradient(skyColors),
                    topLeft = Offset(0f, 0f),
                    size = Size(viewW, horizonY)
                )

                // Draw atmospheric moon/overcast glowing sun
                drawCircle(
                    color = when (carConfig.targetWeather) {
                        "RAIN" -> Color(0x1138BDF8)
                        "SNOW" -> Color(0x1FCCD1D9)
                        else -> Color(0x1A0284C7) // cyan blue neon sun
                    },
                    radius = 90f,
                    center = Offset(viewW * 0.75f, horizonY * 0.4f)
                )

                // 2. Draw Ground Plane
                val groundColor = if (isWinter) Color(0xFFE2E8F0) else Color(0xFF131E12)
                drawRect(
                    color = groundColor,
                    topLeft = Offset(0f, horizonY),
                    size = Size(viewW, viewH - horizonY)
                )

                // Draw perspective dirt patch stripes if on ground
                if (!isWinter) {
                    for (i in 0..12) {
                        val stripeY = horizonY + (viewH - horizonY) * (i / 12f)
                        val rZ = (85f * F) / (stripeY - horizonY) // depth estimation
                        if (rZ > 10.0f) {
                            val pattern = (System.currentTimeMillis() / 200L + i) % 3
                            if (pattern == 0L) {
                                drawLine(
                                    color = Color(0x220B1C0B),
                                    start = Offset(0f, stripeY),
                                    end = Offset(viewW, stripeY),
                                    strokeWidth = (2.0f * F) / rZ
                                )
                            }
                        }
                    }
                } else {
                    // Draw frosty lines for winter road sliding feeling
                    for (i in 0..8) {
                        val stripeY = horizonY + (viewH - horizonY) * (i / 8f)
                        drawLine(
                            color = Color(0xFFCBD5E1),
                            start = Offset(0f, stripeY),
                            end = Offset(viewW, stripeY),
                            strokeWidth = 2f
                        )
                    }
                }

                // 3. Draw Roads (Avenues) in 3D Perspective!
                val roadWidthVal = 190.0
                // Find and project road segments near player
                for (rdIdx in 0..4) {
                    val roadCenterVal = rdIdx * 800.0 + 400.0
                    val roadColor = if (isWinter) Color(0xFF334155) else Color(0xFF1E293B)

                    // Draw North-South vertical road segment
                    // We span from y = py - 1200 to y = py + 1600
                    val startY = (py - 1200.0).coerceAtLeast(0.0)
                    val endY = (py + 1600.0).coerceAtMost(viewModel.mapSize)
                    var segY = startY
                    while (segY < endY) {
                        val nextSegY = segY + 120.0
                        val p1 = projectGround(roadCenterVal - roadWidthVal / 2.0, segY, 0.0)
                        val p2 = projectGround(roadCenterVal + roadWidthVal / 2.0, segY, 0.0)
                        val p3 = projectGround(roadCenterVal + roadWidthVal / 2.0, nextSegY, 0.0)
                        val p4 = projectGround(roadCenterVal - roadWidthVal / 2.0, nextSegY, 0.0)

                        if (p1 != null && p2 != null && p3 != null && p4 != null) {
                            val roadPath = Path().apply {
                                moveTo(p1.x, p1.y)
                                lineTo(p2.x, p2.y)
                                lineTo(p3.x, p3.y)
                                lineTo(p4.x, p4.y)
                                close()
                            }
                            drawPath(roadPath, roadColor)

                            // Center dashed lane markings
                            val midY1 = segY + 20.0
                            val midY2 = segY + 60.0
                            val mp1 = projectGround(roadCenterVal, midY1, 1.0)
                            val mp2 = projectGround(roadCenterVal, midY2, 1.0)
                            if (mp1 != null && mp2 != null) {
                                val distFromCam = (roadCenterVal - px) * kotlin.math.cos(pAngle) + (midY1 - py) * kotlin.math.sin(pAngle) + D_behind
                                if (distFromCam > 5.0) {
                                    val wMark = (3.5f * F) / distFromCam.toFloat()
                                    drawLine(
                                        color = Color(0xFFFBBF24),
                                        start = mp1,
                                        end = mp2,
                                        strokeWidth = wMark.coerceIn(1.5f, 15f)
                                    )
                                }
                            }
                        }
                        segY += 120.0
                    }

                    // Draw East-West Horizontal road segment
                    val startX = (px - 1200.0).coerceAtLeast(0.0)
                    val endX = (px + 1600.0).coerceAtMost(viewModel.mapSize)
                    var segX = startX
                    while (segX < endX) {
                        val nextSegX = segX + 120.0
                        val p1 = projectGround(segX, roadCenterVal - roadWidthVal / 2.0, 0.0)
                        val p2 = projectGround(nextSegX, roadCenterVal - roadWidthVal / 2.0, 0.0)
                        val p3 = projectGround(nextSegX, roadCenterVal + roadWidthVal / 2.0, 0.0)
                        val p4 = projectGround(segX, roadCenterVal + roadWidthVal / 2.0, 0.0)

                        if (p1 != null && p2 != null && p3 != null && p4 != null) {
                            val roadPath = Path().apply {
                                moveTo(p1.x, p1.y)
                                lineTo(p2.x, p2.y)
                                lineTo(p3.x, p3.y)
                                lineTo(p4.x, p4.y)
                                close()
                            }
                            drawPath(roadPath, roadColor)

                            // Dash markings
                            val midX1 = segX + 20.0
                            val midX2 = segX + 60.0
                            val mp1 = projectGround(midX1, roadCenterVal, 1.0)
                            val mp2 = projectGround(midX2, roadCenterVal, 1.0)
                            if (mp1 != null && mp2 != null) {
                                val distFromCam = (midX1 - px) * kotlin.math.cos(pAngle) + (roadCenterVal - py) * kotlin.math.sin(pAngle) + D_behind
                                if (distFromCam > 5.0) {
                                    val wMark = (3.5f * F) / distFromCam.toFloat()
                                    drawLine(
                                        color = Color(0xFFFBBF24),
                                        start = mp1,
                                        end = mp2,
                                        strokeWidth = wMark.coerceIn(1.5f, 15f)
                                    )
                                }
                            }
                        }
                        segX += 120.0
                    }
                }

                // 4. Draw Drift Skidmarks in 3D perspective
                state.driftTrails.forEach { drift ->
                    val proj = projectGround(drift.x.toDouble(), drift.y.toDouble(), 0.0)
                    if (proj != null) {
                        val depthZ = (drift.x - px) * kotlin.math.cos(pAngle) + (drift.y - py) * kotlin.math.sin(pAngle) + D_behind
                        if (depthZ > 8.0) {
                            val rad = (5.0f * F) / depthZ.toFloat()
                            drawCircle(
                                color = Color.Black.copy(alpha = drift.alpha * 0.7f),
                                radius = rad.coerceIn(0.5f, 12f),
                                center = proj
                            )
                        }
                    }
                }

                // 5. Draw 3D Concrete Buildings, Garages and lakes
                val renderedObstacles = mapObstacles.mapNotNull { obs ->
                    val obsCX = (obs.rect.left + obs.rect.right) / 2.0
                    val obsCY = (obs.rect.top + obs.rect.bottom) / 2.0
                    val distZ = (obsCX - px) * kotlin.math.cos(pAngle) + (obsCY - py) * kotlin.math.sin(pAngle) + D_behind
                    
                    if (distZ > 10.0 && distZ < 1900.0) {
                        Pair(obs, distZ)
                    } else {
                        null
                    }
                }.sortedByDescending { it.second } // Painter's order (furthest first)

                renderedObstacles.forEach { (obs, depthZ) ->
                    val heightVal = when (obs.name) {
                        "9-Этажка Панельная" -> 360.0
                        "Панелька Хрущевка" -> 210.0
                        "Тюнинг-Про Автосервис" -> 110.0
                        "Гаражный Кооператив" -> 110.0
                        else -> 120.0
                    }

                    if (obs.type == ObstacleType.WATER) {
                        // Flat icy blue lake
                        val p1 = projectGround(obs.rect.left.toDouble(), obs.rect.top.toDouble(), 1.0)
                        val p2 = projectGround(obs.rect.right.toDouble(), obs.rect.top.toDouble(), 1.0)
                        val p3 = projectGround(obs.rect.right.toDouble(), obs.rect.bottom.toDouble(), 1.0)
                        val p4 = projectGround(obs.rect.left.toDouble(), obs.rect.bottom.toDouble(), 1.0)

                        if (p1 != null && p2 != null && p3 != null && p4 != null) {
                            val lakePath = Path().apply {
                                moveTo(p1.x, p1.y)
                                lineTo(p2.x, p2.y)
                                lineTo(p3.x, p3.y)
                                lineTo(p4.x, p4.y)
                                close()
                            }
                            drawPath(lakePath, if (isWinter) Color(0xFF0284C7) else Color(0xFF0C4A6E))
                            
                            drawLine(
                                color = Color(0xFF38BDF8),
                                start = p1,
                                end = p3,
                                strokeWidth = 1.5f
                            )
                        }
                    } else if (obs.type == ObstacleType.TREE) {
                        // Draw beautiful 3D Siberian Pine or Russian Birch!
                        val tx = (obs.rect.left + obs.rect.right) / 2.0
                        val ty = (obs.rect.top + obs.rect.bottom) / 2.0
                        
                        val isBirch = obs.name.contains("Березка")
                        
                        // Dimensions setup
                        val trunkHeight = if (isBirch) 30.0 else 24.0
                        val crownHeight1 = if (isBirch) 56.0 else 46.0
                        val crownHeight2 = if (isBirch) 78.0 else 66.0
                        
                        val basePt = projectGround(tx, ty, 0.0)
                        val midPt = projectGround(tx, ty, trunkHeight)
                        val topPt = projectGround(tx, ty, crownHeight1)
                        val peakPt = projectGround(tx, ty, crownHeight2)
                        
                        if (basePt != null && midPt != null) {
                            val distZ = (tx - px) * kotlin.math.cos(pAngle) + (ty - py) * kotlin.math.sin(pAngle) + D_behind
                            if (distZ > 5.0) {
                                val trunkW = (if (isBirch) 3.5f else 5.2f) * F / distZ.toFloat()
                                val crownR1 = (if (isBirch) 22.0f else 26.0f) * F / distZ.toFloat()
                                val crownR2 = (if (isBirch) 14.0f else 17.0f) * F / distZ.toFloat()
                                
                                // Draw tree trunk
                                drawLine(
                                    color = if (isBirch) Color.White else Color(0xFF78350F), // white bark for birch, dark trunk for pine
                                    start = basePt,
                                    end = midPt,
                                    strokeWidth = trunkW.coerceIn(1.5f, 25f)
                                )
                                
                                // Decorative birch black stripes
                                if (isBirch) {
                                    val stripeStep = (midPt.y - basePt.y) / 4.0f
                                    for (stepIdx in 1..3) {
                                        val sy = basePt.y + stripeStep * stepIdx
                                        drawLine(
                                            color = Color.Black,
                                            start = Offset(basePt.x - trunkW * 0.44f, sy),
                                            end = Offset(basePt.x + trunkW * 0.44f, sy),
                                            strokeWidth = 1.5f
                                        )
                                    }
                                }
                                
                                // Draw lower foliage level
                                if (topPt != null) {
                                    drawCircle(
                                        color = if (isBirch) Color(0xFF22C55E) else Color(0xFF14532D), // Birch: bright green, Pine: deep forest pine green
                                        radius = crownR1.coerceIn(3.0f, 130.0f),
                                        center = topPt
                                    )
                                }
                                
                                // Draw upper foliage level
                                if (peakPt != null) {
                                    drawCircle(
                                        color = if (isBirch) Color(0xFF4ADE80) else Color(0xFF16A34A), // Birch: lime green, Pine: pure green
                                        radius = crownR2.coerceIn(2.0f, 95.0f),
                                        center = peakPt
                                    )
                                }
                            }
                        }
                    } else {
                        // Render 3D solid box building!
                        val l = obs.rect.left.toDouble()
                        val r = obs.rect.right.toDouble()
                        val t = obs.rect.top.toDouble()
                        val b = obs.rect.bottom.toDouble()

                        // 8 vertices
                        val b1 = projectGround(l, t, 0.0)
                        val b2 = projectGround(r, t, 0.0)
                        val b3 = projectGround(r, b, 0.0)
                        val b4 = projectGround(l, b, 0.0)

                        val t1 = projectGround(l, t, heightVal)
                        val t2 = projectGround(r, t, heightVal)
                        val t3 = projectGround(r, b, heightVal)
                        val t4 = projectGround(l, b, heightVal)

                        // Calculate face depths
                        val wallNorthZ = ((l+r)/2 - px) * kotlin.math.cos(pAngle) + (t - py) * kotlin.math.sin(pAngle) + D_behind
                        val wallEastZ = (r - px) * kotlin.math.cos(pAngle) + ((t+b)/2 - py) * kotlin.math.sin(pAngle) + D_behind
                        val wallSouthZ = ((l+r)/2 - px) * kotlin.math.cos(pAngle) + (b - py) * kotlin.math.sin(pAngle) + D_behind
                        val wallWestZ = (l - px) * kotlin.math.cos(pAngle) + ((t+b)/2 - py) * kotlin.math.sin(pAngle) + D_behind

                        val buildingColorCode = if (obs.name.contains("Тюнинг-Про")) Color(0xFF0891B2) else Color(0xFF475569) // Cyan workshop or Slate apartment

                        // Define faces
                        val faces = mutableListOf<Triple<String, Double, List<Offset?>>>()
                        // Roof face
                        val roofCenterZ = ((l+r)/2 - px) * kotlin.math.cos(pAngle) + ((t+b)/2 - py) * kotlin.math.sin(pAngle) + D_behind
                        faces.add(Triple("ROOF", roofCenterZ, listOf(t1, t2, t3, t4)))
                        // Sides
                        faces.add(Triple("NORTH", wallNorthZ, listOf(b1, b2, t2, t1)))
                        faces.add(Triple("EAST", wallEastZ, listOf(b2, b3, t3, t2)))
                        faces.add(Triple("SOUTH", wallSouthZ, listOf(b3, b4, t4, t3)))
                        faces.add(Triple("WEST", wallWestZ, listOf(b4, b1, t1, t4)))

                        // Sort faces furthest first from camera view
                        faces.sortByDescending { it.second }

                        faces.forEach { (faceName, _, pts) ->
                            if (pts.all { it != null }) {
                                val s1 = pts[0]!!
                                val s2 = pts[1]!!
                                val s3 = pts[2]!!
                                val s4 = pts[3]!!

                                val polyPath = Path().apply {
                                    moveTo(s1.x, s1.y)
                                    lineTo(s2.x, s2.y)
                                    lineTo(s3.x, s3.y)
                                    lineTo(s4.x, s4.y)
                                    close()
                                }

                                val faceColor = when (faceName) {
                                    "ROOF" -> Color(0xFF1E293B)
                                    "NORTH", "SOUTH" -> buildingColorCode.copy(alpha = 0.95f)
                                    else -> buildingColorCode.copy(alpha = 0.82f)
                                }
                                drawPath(polyPath, faceColor)

                                // Draw solid borders
                                drawLine(color = Color(0xFF0F172A), start = s1, end = s2, strokeWidth = 2f)
                                drawLine(color = Color(0xFF0F172A), start = s2, end = s3, strokeWidth = 2f)
                                drawLine(color = Color(0xFF0F172A), start = s3, end = s4, strokeWidth = 2f)
                                drawLine(color = Color(0xFF0F172A), start = s4, end = s1, strokeWidth = 2f)

                                // Rows of windows
                                if (faceName != "ROOF" && !obs.name.contains("Тюнинг-Про") && !obs.name.contains("Кооператив")) {
                                    val columns = 4
                                    val floors = if (obs.name.contains("9-Этажка")) 7 else 4
                                    
                                    for (col in 0 until columns) {
                                        for (flr in 0 until floors) {
                                            val cxFrac1 = (col + 0.3f) / columns.toFloat()
                                            val cxFrac2 = (col + 0.7f) / columns.toFloat()
                                            val cyFrac1 = (flr + 0.3f) / floors.toFloat()
                                            val cyFrac2 = (flr + 0.7f) / floors.toFloat()

                                            val isLit = (col + flr + obs.rect.left.toInt() / 40) % 3 != 0
                                            val winColor = if (isLit) Color(0xFFFEF08A).copy(alpha = 0.8f) else Color(0x7F1E293B)

                                            val wx1 = s1.x + (s2.x - s1.x) * cxFrac1 + (s4.x - s1.x) * cyFrac1
                                            val wy1 = s1.y + (s2.y - s1.y) * cxFrac1 + (s4.y - s1.y) * cyFrac1
                                            val wx2 = s1.x + (s2.x - s1.x) * cxFrac2 + (s4.x - s1.x) * cyFrac2
                                            val wy2 = s1.y + (s2.y - s1.y) * cxFrac2 + (s4.y - s1.y) * cyFrac2

                                            drawCircle(
                                                color = winColor,
                                                radius = maxOf(kotlin.math.abs(wx2 - wx1) * 0.3f, 1f),
                                                center = Offset((wx1 + wx2)/2f, (wy1 + wy2)/2f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 6. Draw floating Pickups (items) in 3D perspective
                state.items.forEach { item ->
                    if (!item.isCollected) {
                        val animateFloat = 6.0 + 3.5 * kotlin.math.sin((System.currentTimeMillis() + item.id * 180L) / 160.0)
                        val proj = projectGround(item.x, item.y, animateFloat)
                        if (proj != null) {
                            val distZ = (item.x - px) * kotlin.math.cos(pAngle) + (item.y - py) * kotlin.math.sin(pAngle) + D_behind
                            if (distZ > 10.0 && distZ < 1200.0) {
                                val itemS3D = (16.0f * F) / distZ.toFloat()
                                val rad3D = itemS3D.coerceIn(2f, 32f)

                                when (item.type) {
                                    ItemType.COIN -> {
                                        drawCircle(
                                            color = Color(0xFF22C55E),
                                            radius = rad3D,
                                            center = proj
                                        )
                                        drawCircle(
                                            color = Color.White,
                                            radius = rad3D * 0.7f,
                                            center = proj,
                                            style = Stroke(width = maxOf(rad3D * 0.15f, 1f))
                                        )
                                    }
                                    ItemType.REPAIR -> {
                                        drawRect(
                                            color = Color(0xFFEF4444),
                                            topLeft = Offset(proj.x - rad3D, proj.y - rad3D),
                                            size = Size(rad3D * 2, rad3D * 2)
                                        )
                                        drawLine(
                                            color = Color.White,
                                            start = Offset(proj.x - rad3D * 0.6f, proj.y),
                                            end = Offset(proj.x + rad3D * 0.6f, proj.y),
                                            strokeWidth = maxOf(rad3D * 0.2f, 1.5f)
                                        )
                                        drawLine(
                                            color = Color.White,
                                            start = Offset(proj.x, proj.y - rad3D * 0.6f),
                                            end = Offset(proj.x, proj.y + rad3D * 0.6f),
                                            strokeWidth = maxOf(rad3D * 0.2f, 1.5f)
                                        )
                                    }
                                    ItemType.NITRO -> {
                                        drawCircle(
                                            color = Color(0xFF00D2FF),
                                            radius = rad3D,
                                            center = proj
                                        )
                                        drawCircle(
                                            color = Color.White,
                                            radius = rad3D * 0.5f,
                                            center = proj
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 7. Bullet Tracers in 3D space!
                state.bullets.forEach { bullet ->
                    val bpStart = projectGround(bullet.x, bullet.y, 4.0)
                    val bpEnd = projectGround(bullet.x - bullet.vx * 1.3, bullet.y - bullet.vy * 1.3, 4.0)
                    if (bpStart != null && bpEnd != null) {
                        drawLine(
                            color = Color(0xFFF97316),
                            start = bpStart,
                            end = bpEnd,
                            strokeWidth = 3f
                        )
                    }
                }

                // 8. Police Cars in 3D Perspective!
                state.copCars.forEach { cop ->
                    val distZ = (cop.x - px) * kotlin.math.cos(pAngle) + (cop.y - py) * kotlin.math.sin(pAngle) + D_behind
                    if (distZ > 10.0 && distZ < 1400.0) {
                        val sizeS3D = (34.0f * F) / distZ.toFloat()
                        val copW = sizeS3D.coerceIn(2f, 100f)
                        val copH = (copW * 0.45f).coerceIn(1f, 45f)

                        val copProj = projectGround(cop.x, cop.y, 1.0)
                        if (copProj != null) {
                            val isFlashingSirenRed = (System.currentTimeMillis() / 150L) % 2L == 0L
                            val copBodyColor = if (cop.isStruck) Color(0xFFFECDD3) else Color(0xFF1E293B)

                            drawRect(
                                color = Color.Black,
                                topLeft = Offset(copProj.x - copW * 0.45f, copProj.y - copH * 0.1f),
                                size = Size(copW * 0.2f, copH * 0.3f)
                            )
                            drawRect(
                                color = Color.Black,
                                topLeft = Offset(copProj.x + copW * 0.25f, copProj.y - copH * 0.1f),
                                size = Size(copW * 0.2f, copH * 0.3f)
                            )

                            drawRoundRect(
                                color = copBodyColor,
                                topLeft = Offset(copProj.x - copW * 0.5f, copProj.y - copH),
                                size = Size(copW, copH),
                                cornerRadius = CornerRadius(4f, 4f)
                            )

                            drawRect(
                                color = Color.White,
                                topLeft = Offset(copProj.x - copW * 0.18f, copProj.y - copH),
                                size = Size(copW * 0.36f, copH)
                            )

                            drawRect(
                                color = Color(0xFF1D4ED8),
                                topLeft = Offset(copProj.x - copW * 0.5f, copProj.y - copH * 0.65f),
                                size = Size(copW, copH * 0.25f)
                            )

                            drawRect(
                                color = Color(0xFF0F172A),
                                topLeft = Offset(copProj.x - copW * 0.35f, copProj.y - copH * 0.95f),
                                size = Size(copW * 0.7f, copH * 0.3f)
                            )

                            val sirenY = copProj.y - copH - copH * 0.15f
                            drawRect(
                                color = if (isFlashingSirenRed) Color.Red else Color.Blue,
                                topLeft = Offset(copProj.x - copW * 0.12f, sirenY),
                                size = Size(copW * 0.12f, copH * 0.22f)
                            )
                            drawRect(
                                color = if (isFlashingSirenRed) Color.Blue else Color.Red,
                                topLeft = Offset(copProj.x, sirenY),
                                size = Size(copW * 0.12f, copH * 0.22f)
                            )
                        }
                    }
                }

                // 9. Draw Player's Dynamic Vehicle Model in 3D Chase View!
                if (!isInterior) {
                    val lZ = D_behind.toDouble() - 15.0
                val tiltX = -viewModel.steerInput * 16.0
                
                // Slammed Priora sits much lower
                val ldaCY = if (carConfig.carModelIndex == 2) -4.0 else -14.0

                val isKamaz = carConfig.carModelIndex == 3
                val sizeLada3D = (if (isKamaz) 78.0f else 54.0f) * F / lZ.toFloat()
                val ldaW = sizeLada3D.coerceIn(10f, 180f)
                val ldaH = (ldaW * (if (isKamaz) 0.85f else 0.46f)).coerceIn(4f, 130f)

                if (carConfig.neonUnderglow) {
                    drawCircle(
                        color = Color(0xFF22C55E).copy(alpha = 0.45f),
                        radius = ldaW * 0.8f,
                        center = Offset(cx + tiltX.toFloat(), cy - ldaCY.toFloat() - ldaH * 0.2f)
                    )
                }

                val pLocX = cx + tiltX.toFloat()
                val pLocY = cy - ldaCY.toFloat()

                if (state.playerSpeed > 0.1 && viewModel.nitroActive && state.nitroAmount > 0.0) {
                    val flmVal = 20f + (System.currentTimeMillis() % 40L)
                    val flamePath = Path().apply {
                        moveTo(pLocX - ldaW * 0.35f, pLocY + ldaH * 0.2f)
                        lineTo(pLocX - ldaW * 0.35f - flmVal, pLocY + ldaH * 0.2f)
                        lineTo(pLocX - ldaW * 0.35f, pLocY + ldaH * 0.1f)
                        close()
                    }
                    drawPath(flamePath, Color(0xFF00D2FF))
                }

                // Black Tires
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(pLocX - ldaW * 0.44f, pLocY - ldaH * 0.15f),
                    size = Size(ldaW * 0.2f, ldaH * 0.35f)
                )
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(pLocX + ldaW * 0.24f, pLocY - ldaH * 0.15f),
                    size = Size(ldaW * 0.2f, ldaH * 0.35f)
                )

                if (isKamaz) {
                    // Draw KAMAZ heavy cabin box body
                    drawRoundRect(
                        color = Color(paintCarColor),
                        topLeft = Offset(pLocX - ldaW * 0.48f, pLocY - ldaH),
                        size = Size(ldaW * 0.96f, ldaH),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    // High rectangular windscreen splits (Kamaz styling)
                    val ldaWindshield = Path().apply {
                        moveTo(pLocX - ldaW * 0.42f, pLocY - ldaH * 0.90f)
                        lineTo(pLocX + ldaW * 0.42f, pLocY - ldaH * 0.90f)
                        lineTo(pLocX + ldaW * 0.42f, pLocY - ldaH * 0.52f)
                        lineTo(pLocX - ldaW * 0.42f, pLocY - ldaH * 0.52f)
                        close()
                    }
                    drawPath(ldaWindshield, Color(0xFF0F172A))
                    // Center strut line for split windshield
                    drawLine(
                        color = Color(paintCarColor),
                        start = Offset(pLocX, pLocY - ldaH * 0.90f),
                        end = Offset(pLocX, pLocY - ldaH * 0.52f),
                        strokeWidth = 3f
                    )
                    // Giant yellow fog lights & taillights
                    drawCircle(color = Color(0xFFFBBF24), radius = ldaW * 0.08f, center = Offset(pLocX - ldaW * 0.36f, pLocY - ldaH * 0.22f))
                    drawCircle(color = Color(0xFFFBBF24), radius = ldaW * 0.08f, center = Offset(pLocX + ldaW * 0.36f, pLocY - ldaH * 0.22f))
                    drawRect(color = Color(0xFFEF4444), topLeft = Offset(pLocX - ldaW * 0.46f, pLocY - ldaH * 0.40f), size = Size(ldaW * 0.12f, ldaH * 0.12f))
                    drawRect(color = Color(0xFFEF4444), topLeft = Offset(pLocX + ldaW * 0.34f, pLocY - ldaH * 0.40f), size = Size(ldaW * 0.12f, ldaH * 0.12f))
                } else {
                    // Draw Hatchback/Sedan dynamic outline
                    drawRoundRect(
                        color = Color(paintCarColor),
                        topLeft = Offset(pLocX - ldaW * 0.50f, pLocY - ldaH),
                        size = Size(ldaW, ldaH),
                        cornerRadius = CornerRadius(6f, 6f)
                    )

                    val ldaWindshield = Path().apply {
                        moveTo(pLocX - ldaW * 0.36f, pLocY - ldaH * 0.92f)
                        lineTo(pLocX + ldaW * 0.36f, pLocY - ldaH * 0.92f)
                        lineTo(pLocX + ldaW * 0.43f, pLocY - ldaH * 0.58f)
                        lineTo(pLocX - ldaW * 0.43f, pLocY - ldaH * 0.58f)
                        close()
                    }
                    drawPath(ldaWindshield, Color(0xFF0F172A))

                    // Tail lights
                    drawRect(
                        color = Color(0xFFEF4444),
                        topLeft = Offset(pLocX - ldaW * 0.47f, pLocY - ldaH * 0.45f),
                        size = Size(ldaW * 0.18f, ldaH * 0.18f)
                    )
                    drawRect(
                        color = Color(0xFFEF4444),
                        topLeft = Offset(pLocX + ldaW * 0.29f, pLocY - ldaH * 0.45f),
                        size = Size(ldaW * 0.18f, ldaH * 0.18f)
                    )

                    // Rear bumper trim line
                    drawLine(
                        color = Color(0xFF94A3B8),
                        start = Offset(pLocX - ldaW * 0.35f, pLocY - ldaH * 0.15f),
                        end = Offset(pLocX + ldaW * 0.35f, pLocY - ldaH * 0.15f),
                        strokeWidth = 2f
                    )

                    // Big spoiler custom wings
                    if (carConfig.bigSpoiler) {
                        drawLine(
                            color = Color.White,
                            start = Offset(pLocX - ldaW * 0.4f, pLocY - ldaH * 0.92f),
                            end = Offset(pLocX - ldaW * 0.4f, pLocY - ldaH * 1.15f),
                            strokeWidth = 3f
                        )
                        drawLine(
                            color = Color.White,
                            start = Offset(pLocX + ldaW * 0.4f, pLocY - ldaH * 0.92f),
                            end = Offset(pLocX + ldaW * 0.4f, pLocY - ldaH * 1.15f),
                            strokeWidth = 3f
                        )
                        drawLine(
                            color = Color(paintCarColor),
                            start = Offset(pLocX - ldaW * 0.46f, pLocY - ldaH * 1.15f),
                            end = Offset(pLocX + ldaW * 0.46f, pLocY - ldaH * 1.15f),
                            strokeWidth = 5f
                        )
                    }
                }
                }

                // 10. Draw 3D Falling Weather Overlays (RAIN/SNOW)
                if (carConfig.targetWeather == "RAIN") {
                    val frameTick = System.currentTimeMillis() / 15f
                    for (k in 0..40) {
                        val rx = (k * 187 + frameTick * 0.6f) % viewW
                        val ry = (k * 229 + frameTick * 4.2f) % (viewH - horizonY) + horizonY
                        drawLine(
                            color = Color(0x663B82F6),
                            start = Offset(rx, ry),
                            end = Offset(rx - 8f, ry + 16f),
                            strokeWidth = 1.5f
                        )
                    }
                } else if (carConfig.targetWeather == "SNOW") {
                    val frameTick = System.currentTimeMillis() / 20f
                    for (k in 0..35) {
                        val sx = (k * 157 + frameTick * 1.2f) % viewW
                        val sy = (k * 313 + frameTick * 2.2f) % (viewH - horizonY) + horizonY
                        drawCircle(
                            color = Color(0xECE2E8F0),
                            radius = 3f + (k % 3),
                            center = Offset(sx, sy)
                        )
                    }
                }

                // 11. Draw Custom Driver Cockpit (Car Interior) Overlay if in Interior View!
                if (isInterior) {
                    val dashTopY = viewH * 0.52f
                    
                    // A. Pillar frames (A-columns left & right to framework the windscreen view)
                    val leftPillar = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(viewW * 0.12f, 0f)
                        lineTo(viewW * 0.22f, dashTopY)
                        lineTo(0f, dashTopY)
                        close()
                    }
                    val rightPillar = Path().apply {
                        moveTo(viewW, 0f)
                        lineTo(viewW * 0.88f, 0f)
                        lineTo(viewW * 0.78f, dashTopY)
                        lineTo(viewW, dashTopY)
                        close()
                    }
                    val roofHeaddown = Path().apply {
                        moveTo(0f, 0f)
                        lineTo(viewW, 0f)
                        lineTo(viewW, viewH * 0.08f)
                        lineTo(0f, viewH * 0.08f)
                        close()
                    }
                    drawPath(leftPillar, Color(0xFF1E293B).copy(alpha = 0.95f))
                    drawPath(rightPillar, Color(0xFF1E293B).copy(alpha = 0.95f))
                    drawPath(roofHeaddown, Color(0xFF0F172A).copy(alpha = 0.98f))

                    // B. Rearview mirror (Functional)
                    val rvmX = viewW * 0.5f
                    val rvmY = viewH * 0.12f
                    val rvmW = 160f
                    val rvmH = 44f
                    // Draw mirror rim mount
                    drawLine(
                        color = Color(0xFF0F172A),
                        start = Offset(rvmX, viewH * 0.08f),
                        end = Offset(rvmX, rvmY - rvmH/2),
                        strokeWidth = 6f
                    )
                    // Mirror rim casing
                    drawRoundRect(
                        color = Color(0xFF1E293B),
                        topLeft = Offset(rvmX - rvmW / 2 - 4f, rvmY - rvmH / 2 - 4f),
                        size = Size(rvmW + 8f, rvmH + 8f),
                        cornerRadius = CornerRadius(5f, 5f)
                    )
                    // Mirror glass itself (reflects behind road)
                    drawRoundRect(
                        color = Color(0xFF334155),
                        topLeft = Offset(rvmX - rvmW / 2, rvmY - rvmH / 2),
                        size = Size(rvmW, rvmH),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    // Draw perspective road and center lines behind us in mirror view
                    drawLine(
                        color = Color(0x66EAB308),
                        start = Offset(rvmX + (viewModel.steerInput * 16).toFloat(), rvmY + rvmH / 2 - 2f),
                        end = Offset(rvmX - (viewModel.steerInput * 8).toFloat(), rvmY - rvmH / 2 + 2f),
                        strokeWidth = 3f
                    )
                    // Mini police chasing in the mirror
                    if (state.copCars.isNotEmpty()) {
                        val isBlink = (System.currentTimeMillis() / 150L) % 2L == 0L
                        // Draw mini cop car block inside mirror
                        drawRect(
                            color = Color(0xFF020617),
                            topLeft = Offset(rvmX - 18f, rvmY - 8f),
                            size = Size(36f, 16f)
                        )
                        // Tiny flashing red/blue headlights
                        drawCircle(
                            color = if (isBlink) Color.Red else Color.Blue,
                            radius = 4f,
                            center = Offset(rvmX - 10f, rvmY)
                        )
                        drawCircle(
                            color = if (isBlink) Color.Blue else Color.Red,
                            radius = 4f,
                            center = Offset(rvmX + 10f, rvmY)
                        )
                    }

                    // Sway multiplier for hanging toys based on steering momentum and continuous vibration
                    val swayDeg = (viewModel.steerInput * 22.0 + kotlin.math.sin(System.currentTimeMillis() / 180.0) * 5.0).toFloat()

                    // C. Main Dashboard Panel and Car-Model Specific Styling
                    when (carConfig.carModelIndex) {
                        0 -> { // ВАЗ-2106 «Шоха»
                            // Draw primary black vintage vinyl console base
                            drawRect(
                                color = Color(0xFF1A1A1A),
                                topLeft = Offset(0f, dashTopY),
                                size = Size(viewW, viewH - dashTopY)
                            )
                            // Elegant cherry redwood mahogany wood veneer stripe
                            drawRect(
                                color = Color(0xFF7C2D12),
                                topLeft = Offset(0f, dashTopY + 14f),
                                size = Size(viewW, 36f)
                            )
                            // Chrome dividers
                            drawLine(color = Color(0xFF94A3B8), start = Offset(0f, dashTopY + 14f), end = Offset(viewW, dashTopY + 14f), strokeWidth = 2.5f)
                            drawLine(color = Color(0xFF94A3B8), start = Offset(0f, dashTopY + 50f), end = Offset(viewW, dashTopY + 50f), strokeWidth = 2.5f)

                            // Retro Hanging Orthodox Icons ("Тройник икон") on the dash center top
                            val tX = viewW * 0.54f
                            val tY = dashTopY - 26f
                            // Small wooden stand frame
                            drawRoundRect(color = Color(0xFF451A03), topLeft = Offset(tX - 35f, tY), size = Size(70f, 22f), cornerRadius = CornerRadius(4f,4f))
                            // Three miniature golden icons side by side
                            drawRect(color = Color(0xFFF59E0B), topLeft = Offset(tX - 30f, tY + 3f), size = Size(16f, 16f))
                            drawRect(color = Color(0xFFF59E0B), topLeft = Offset(tX - 8f, tY + 3f), size = Size(16f, 16f))
                            drawRect(color = Color(0xFFF59E0B), topLeft = Offset(tX + 14f, tY + 3f), size = Size(16f, 16f))
                            // Gold cross outlines
                            drawCircle(color = Color.Red, radius = 2f, center = Offset(tX - 22f, tY + 11f))
                            drawCircle(color = Color.Red, radius = 2f, center = Offset(tX, tY + 11f))
                            drawCircle(color = Color.Red, radius = 2f, center = Offset(tX + 22f, tY + 11f))

                            // Large vintage circular Gauge Dials
                            val sMeterCX = viewW * 0.22f
                            val dialCY = dashTopY + 95f
                            // Speedometer Outer Chrome Outer
                            drawCircle(color = Color(0xFF94A3B8), radius = 48f, center = Offset(sMeterCX, dialCY))
                            drawCircle(color = Color.Black, radius = 44f, center = Offset(sMeterCX, dialCY))
                            // Ticks
                            for (tIdx in 0..10) {
                                val deg = -120f + tIdx * 24f
                                rotate(degrees = deg, pivot = Offset(sMeterCX, dialCY)) {
                                    drawLine(color = Color.White, start = Offset(sMeterCX, dialCY - 42f), end = Offset(sMeterCX, dialCY - 34f), strokeWidth = 2f)
                                }
                            }
                            // Speed Needle rotation
                            val needleAngle = -120f + (state.playerSpeed.toFloat() / (viewModel.maxSpeed.toFloat() * 1.5f)) * 240f
                            rotate(degrees = needleAngle, pivot = Offset(sMeterCX, dialCY)) {
                                drawLine(color = Color(0xFFEF4444), start = Offset(sMeterCX, dialCY), end = Offset(sMeterCX, dialCY - 39f), strokeWidth = 2.5f)
                            }
                            drawCircle(color = Color.White, radius = 4f, center = Offset(sMeterCX, dialCY))

                            // Tachometer Outer Chrome Ring
                            val tMeterCX = viewW * 0.40f
                            drawCircle(color = Color(0xFF94A3B8), radius = 44f, center = Offset(tMeterCX, dialCY))
                            drawCircle(color = Color.Black, radius = 40f, center = Offset(tMeterCX, dialCY))
                            val tachoAngle = -120f + (kotlin.math.abs(state.playerSpeed).toFloat() / viewModel.maxSpeed.toFloat()) * 180f + (if (viewModel.accelInput > 0) 45f else 15f) + (kotlin.math.sin(System.currentTimeMillis() / 45.0) * 3f).toFloat()
                            rotate(degrees = tachoAngle, pivot = Offset(tMeterCX, dialCY)) {
                                drawLine(color = Color(0xFFFFD700), start = Offset(tMeterCX, dialCY), end = Offset(tMeterCX, dialCY - 35f), strokeWidth = 2.5f)
                            }
                            drawCircle(color = Color.White, radius = 4f, center = Offset(tMeterCX, dialCY))

                            // Dual-spoke thin retro steering wheel
                            val wheelCX = viewW * 0.31f
                            val wheelCY = viewH * 0.83f
                            val wRad = viewW * 0.17f
                            rotate(degrees = viewModel.steerInput.toFloat() * 62f, pivot = Offset(wheelCX, wheelCY)) {
                                // Black outer ring
                                drawCircle(color = Color(0xFF1E293B), radius = wRad, center = Offset(wheelCX, wheelCY), style = Stroke(width = 12f))
                                // Chrome accents
                                drawCircle(color = Color(0xFFCBD5E1), radius = wRad - 4f, center = Offset(wheelCX, wheelCY), style = Stroke(width = 2f))
                                // Dual main horizontal spokes
                                drawLine(color = Color(0xFF1E293B), start = Offset(wheelCX - wRad, wheelCY), end = Offset(wheelCX + wRad, wheelCY), strokeWidth = 10f)
                                // Chrome horn button at the center
                                drawCircle(color = Color(0xFF94A3B8), radius = 22f, center = Offset(wheelCX, wheelCY))
                                drawCircle(color = Color.White, radius = 6f, center = Offset(wheelCX, wheelCY))
                            }
                        }
                        1 -> { // ВАЗ-2114 «Четырка»
                            // Main grey plastic blocky cockpit console
                            drawRect(
                                color = Color(0xFF27272A), // Dark slate zinc grey
                                topLeft = Offset(0f, dashTopY),
                                size = Size(viewW, viewH - dashTopY)
                            )
                            // Combined meter dials box
                            drawRoundRect(
                                color = Color(0xFF18181B),
                                topLeft = Offset(viewW * 0.12f, dashTopY + 45f),
                                size = Size(viewW * 0.38f, 100f),
                                cornerRadius = CornerRadius(6f,6f)
                            )
                            // Electronic digital speedometer display
                            val dialCY = dashTopY + 95f
                            // Speedometer Circular meter left inside box
                            val sX = viewW * 0.22f
                            drawCircle(color = Color.Black, radius = 32f, center = Offset(sX, dialCY))
                            val speedNeedleAng = -120f + (state.playerSpeed.toFloat() / (viewModel.maxSpeed.toFloat() * 1.5f)) * 240f
                            rotate(degrees = speedNeedleAng, pivot = Offset(sX, dialCY)) {
                                drawLine(color = Color(0xFFF97316), start = Offset(sX, dialCY), end = Offset(sX, dialCY - 29f), strokeWidth = 2.5f)
                            }
                            drawCircle(color = Color.White, radius = 3f, center = Offset(sX, dialCY))

                            // Vintage neon green digital clock in center stack
                            val clockX = viewW * 0.58f
                            val clockY = dashTopY + 30f
                            drawRect(color = Color.Black, topLeft = Offset(clockX - 35f, clockY), size = Size(70f, 22f))
                            // Mock green time "14:14"
                            drawRect(color = Color(0xFF22C55E).copy(alpha = 0.85f), topLeft = Offset(clockX - 25f, clockY + 4f), size = Size(10f, 3f))
                            drawRect(color = Color(0xFF22C55E).copy(alpha = 0.85f), topLeft = Offset(clockX - 25f, clockY + 7f), size = Size(3f, 10f))
                            drawRect(color = Color(0xFF22C55E).copy(alpha = 0.85f), topLeft = Offset(clockX - 15f, clockY + 4f), size = Size(10f, 3f))
                            drawRect(color = Color(0xFF22C55E).copy(alpha = 0.85f), topLeft = Offset(clockX - 15f, clockY + 7f), size = Size(3f, 10f))
                            // Dots
                            drawCircle(color = Color(0xFF22C55E), radius = 2.2f, center = Offset(clockX, clockY + 8f))
                            drawCircle(color = Color(0xFF22C55E), radius = 2.2f, center = Offset(clockX, clockY + 14f))
                            
                            // Hanging Fresh-Pine Tree "Ёлочка"
                            val pineX = rvmX - 15f
                            val pineY = rvmY + rvmH / 2 + 10f
                            rotate(degrees = swayDeg, pivot = Offset(pineX, pineY - 8f)) {
                                // Thread line
                                drawLine(color = Color.White, start = Offset(pineX, pineY - 8f), end = Offset(pineX, pineY + 10f), strokeWidth = 1.5f)
                                // Green pine leaves (triangles)
                                val pathPine = Path().apply {
                                    moveTo(pineX, pineY + 10f)
                                    lineTo(pineX - 14f, pineY + 24f)
                                    lineTo(pineX + 14f, pineY + 24f)
                                    close()
                                    moveTo(pineX, pineY + 20f)
                                    lineTo(pineX - 18f, pineY + 38f)
                                    lineTo(pineX + 18f, pineY + 38f)
                                    close()
                                    moveTo(pineX, pineY + 32f)
                                    lineTo(pineX - 22f, pineY + 54f)
                                    lineTo(pineX + 22f, pineY + 54f)
                                    close()
                                }
                                drawPath(pathPine, Color(0xFF16A34A))
                                // Trunk
                                drawRect(color = Color(0xFF78350F), topLeft = Offset(pineX - 4f, pineY + 54f), size = Size(8f, 10f))
                            }

                            // Classic 3-spoke massive plastic steering wheel
                            val wheelCX = viewW * 0.31f
                            val wheelCY = viewH * 0.83f
                            val wRad = viewW * 0.16f
                            rotate(degrees = viewModel.steerInput.toFloat() * 66f, pivot = Offset(wheelCX, wheelCY)) {
                                drawCircle(color = Color(0xFF181811), radius = wRad, center = Offset(wheelCX, wheelCY), style = Stroke(width = 16f))
                                // Spokes
                                drawLine(color = Color(0xFF181811), start = Offset(wheelCX, wheelCY), end = Offset(wheelCX - wRad, wheelCY - 10f), strokeWidth = 15f)
                                drawLine(color = Color(0xFF181811), start = Offset(wheelCX, wheelCY), end = Offset(wheelCX + wRad, wheelCY - 10f), strokeWidth = 15f)
                                drawLine(color = Color(0xFF181811), start = Offset(wheelCX, wheelCY), end = Offset(wheelCX, wheelCY + wRad), strokeWidth = 18f)
                                // Center Boss pad
                                drawCircle(color = Color(0xFF27272A), radius = 26f, center = Offset(wheelCX, wheelCY))
                            }
                        }
                        2 -> { // LADA Priora «Сликер»
                            // Matte dark executive carbon cockpit console
                            drawRect(
                                color = Color(0xFF18181B),
                                topLeft = Offset(0f, dashTopY),
                                size = Size(viewW, viewH - dashTopY)
                            )
                            // Silver line decoration
                            drawLine(color = Color(0xFF94A3B8), start = Offset(0f, dashTopY + 12f), end = Offset(viewW, dashTopY + 12f), strokeWidth = 2f)

                            // Dynamic stitching matching paintCarColor!
                            drawRect(
                                color = Color(paintCarColor).copy(alpha = 0.28f),
                                topLeft = Offset(0f, dashTopY + 28f),
                                size = Size(viewW, 6f)
                            )

                            // Speed radar detector device glowing on top-right dash
                            val detectorX = viewW * 0.72f
                            val detectorY = dashTopY + 10f
                            drawRect(color = Color(0xFF27272A), topLeft = Offset(detectorX - 35f, detectorY), size = Size(70f, 18f))
                            drawRect(color = Color.Black, topLeft = Offset(detectorX - 32f, detectorY + 2f), size = Size(64f, 14f))
                            val isRadarWarn = state.playerSpeed > viewModel.maxSpeed * 0.8
                            drawCircle(
                                color = if (isRadarWarn) Color.Red else Color.Green,
                                radius = 3.5f,
                                center = Offset(detectorX - 22f, detectorY + 9f)
                            )
                            drawRect(color = if (isRadarWarn) Color(0x66EF4444) else Color(0x6622C55E), topLeft = Offset(detectorX - 12f, detectorY + 4f), size = Size(40f, 10f))

                            // Speedometer instrument console glowing
                            val dX = viewW * 0.22f
                            val dialCY = dashTopY + 95f
                            drawCircle(color = Color(0xFF00D2FF), radius = 44f, center = Offset(dX, dialCY), style = Stroke(width = 1.5f))
                            drawCircle(color = Color.Black, radius = 41f, center = Offset(dX, dialCY))
                            val speedNeedlePriora = -120f + (state.playerSpeed.toFloat() / (viewModel.maxSpeed.toFloat() * 1.5f)) * 240f
                            rotate(degrees = speedNeedlePriora, pivot = Offset(dX, dialCY)) {
                                drawLine(color = Color.Red, start = Offset(dX, dialCY), end = Offset(dX, dialCY - 34f), strokeWidth = 2f)
                            }
                            drawCircle(color = Color.White, radius = 3f, center = Offset(dX, dialCY))

                            // Priora style modern thick oval 3-spoke steering wheel
                            val wheelCX = viewW * 0.31f
                            val wheelCY = viewH * 0.83f
                            val wRad = viewW * 0.16f
                            rotate(degrees = viewModel.steerInput.toFloat() * 65f, pivot = Offset(wheelCX, wheelCY)) {
                                drawCircle(color = Color(0xFF09090B), radius = wRad, center = Offset(wheelCX, wheelCY), style = Stroke(width = 18f))
                                drawLine(color = Color(0xFF09090B), start = Offset(wheelCX, wheelCY), end = Offset(wheelCX - wRad, wheelCY + 15f), strokeWidth = 14f)
                                drawLine(color = Color(0xFF09090B), start = Offset(wheelCX, wheelCY), end = Offset(wheelCX + wRad, wheelCY + 15f), strokeWidth = 14f)
                                drawLine(color = Color(0xFF09090B), start = Offset(wheelCX, wheelCY), end = Offset(wheelCX, wheelCY - wRad), strokeWidth = 16f)
                                drawRoundRect(color = Color(0xFF27272A), topLeft = Offset(wheelCX - 22f, wheelCY - 16f), size = Size(44f, 32f), cornerRadius = CornerRadius(8f,8f))
                                drawCircle(color = Color(0xFFCBD5E1), radius = 6f, center = Offset(wheelCX, wheelCY), style = Stroke(width = 1.5f))
                            }
                        }
                        3 -> { // КАМАЗ-54115 «Громобой»
                            // High heavy iron dashboard console
                            drawRect(
                                color = Color(0xFF451A03),
                                topLeft = Offset(0f, dashTopY - 10f),
                                size = Size(viewW, viewH - (dashTopY - 10f))
                            )
                            drawLine(
                                color = Color(0xFF27272A),
                                start = Offset(viewW * 0.5f, 0f),
                                end = Offset(viewW * 0.5f, dashTopY - 10f),
                                strokeWidth = 8f
                            )

                            // Small vibrating Russian Flag
                            val flagX = viewW * 0.5f
                            val flagY = dashTopY - 48f
                            val isVib = (System.currentTimeMillis() / 80L) % 2L == 0L
                            val shiftFlag = if (isVib) 1.5f else 0.0f
                            drawLine(color = Color.Black, start = Offset(flagX, flagY), end = Offset(flagX, flagY + 38f), strokeWidth = 2.5f)
                            drawRect(color = Color.White, topLeft = Offset(flagX + 2f, flagY + 3f), size = Size(32f + shiftFlag, 8f))
                            drawRect(color = Color(0xFF1D4ED8), topLeft = Offset(flagX + 2f, flagY + 11f), size = Size(32f + shiftFlag, 8f))
                            drawRect(color = Color(0xFFEF4444), topLeft = Offset(flagX + 2f, flagY + 19f), size = Size(32f + shiftFlag, 8f))

                            // Retro Soviet hot tea glass in "подстаканник"
                            val teaX = viewW * 0.74f
                            val teaY = dashTopY + 30f
                            val phaseS = (System.currentTimeMillis() / 250f)
                            drawCircle(color = Color.White.copy(alpha = 0.22f), radius = 6f + (phaseS % 3) * 2f, center = Offset(teaX + 5f, teaY - 14f - (phaseS % 12L) * 1.5f))
                            drawCircle(color = Color.White.copy(alpha = 0.22f), radius = 4f + (phaseS % 5) * 1.5f, center = Offset(teaX - 5f, teaY - 24f - (phaseS % 12L) * 1.5f))
                            drawRect(color = Color(0xFF94A3B8), topLeft = Offset(teaX - 22f, teaY), size = Size(44f, 40f))
                            drawRect(color = Color(0xFFB45309), topLeft = Offset(teaX - 16f, teaY + 4f), size = Size(32f, 32f))
                            drawRoundRect(color = Color(0xFF94A3B8), topLeft = Offset(teaX + 20f, teaY + 10f), size = Size(10f, 20f), cornerRadius = CornerRadius(4f,4f), style = Stroke(width = 3f))

                            // Massive round heavy industrial metric dials
                            val dX = viewW * 0.22f
                            val dialCY = dashTopY + 90f
                            drawCircle(color = Color.Black, radius = 40f, center = Offset(dX, dialCY))
                            drawCircle(color = Color.White, radius = 37f, center = Offset(dX, dialCY), style = Stroke(width = 1.5f))
                            val speedNeedleKamaz = -120f + (state.playerSpeed.toFloat() / (viewModel.maxSpeed.toFloat() * 1.5f)) * 240f
                            rotate(degrees = speedNeedleKamaz, pivot = Offset(dX, dialCY)) {
                                drawLine(color = Color.Red, start = Offset(dX, dialCY), end = Offset(dX, dialCY - 28f), strokeWidth = 3f)
                            }
                            drawCircle(color = Color.White, radius = 5f, center = Offset(dX, dialCY))

                            // Giant flat horizontal truck steering wheel
                            val wheelCX = viewW * 0.31f
                            val wheelCY = viewH * 0.83f
                            val wRad = viewW * 0.17f
                            rotate(degrees = viewModel.steerInput.toFloat() * 56f, pivot = Offset(wheelCX, wheelCY)) {
                                drawCircle(color = Color(0xFF1E293B), radius = wRad, center = Offset(wheelCX, wheelCY), style = Stroke(width = 18f))
                                drawLine(color = Color(0xFF1E293B), start = Offset(wheelCX - wRad, wheelCY + 6f), end = Offset(wheelCX + wRad, wheelCY + 6f), strokeWidth = 14f)
                                drawRect(color = Color(0xFF1E293B), topLeft = Offset(wheelCX - 22f, wheelCY - 14f), size = Size(44f, 28f))
                            }
                        }
                        4 -> { // BMW E34 «Бумер ОПГ»
                            val tiltDashPath = Path().apply {
                                moveTo(0f, dashTopY)
                                lineTo(viewW, dashTopY + 12f)
                                lineTo(viewW, viewH)
                                lineTo(0f, viewH)
                                close()
                            }
                            drawPath(tiltDashPath, Color(0xFF0F172A))

                            // M-Performance dynamic color embroidery highlight
                            val embX = viewW * 0.58f
                            drawRect(color = Color(0xFF38BDF8), topLeft = Offset(embX, dashTopY + 18f), size = Size(10f, 4f))
                            drawRect(color = Color(0xFF1D4ED8), topLeft = Offset(embX + 10f, dashTopY + 18f), size = Size(10f, 4f))
                            drawRect(color = Color(0xFFEF4444), topLeft = Offset(embX + 20f, dashTopY + 18f), size = Size(10f, 4f))

                            // Sleek gangster pilot sunglasses
                            val glassX = viewW * 0.72f
                            val glassY = dashTopY + 22f
                            drawCircle(color = Color.Black, radius = 8f, center = Offset(glassX - 10f, glassY))
                            drawCircle(color = Color.Black, radius = 8f, center = Offset(glassX + 10f, glassY))
                            drawLine(color = Color(0xFFFFD700), start = Offset(glassX - 10f, glassY - 5f), end = Offset(glassX + 10f, glassY - 5f), strokeWidth = 2f)

                            // BMW Orange / Amber illuminated clusters
                            val dX = viewW * 0.22f
                            val dialCY = dashTopY + 95f
                            drawCircle(color = Color(0xFFEA580C), radius = 46f, center = Offset(dX, dialCY), style = Stroke(width = 1.6f))
                            drawCircle(color = Color.Black, radius = 43f, center = Offset(dX, dialCY))
                            val speedNeedleBmw = -120f + (state.playerSpeed.toFloat() / (viewModel.maxSpeed.toFloat() * 1.5f)) * 240f
                            rotate(degrees = speedNeedleBmw, pivot = Offset(dX, dialCY)) {
                                drawLine(color = Color(0xFFEA580C), start = Offset(dX, dialCY), end = Offset(dX, dialCY - 34f), strokeWidth = 2.5f)
                            }
                            drawCircle(color = Color.White, radius = 3.5f, center = Offset(dX, dialCY))

                            // Sports M-tech 3-spoke steering wheel
                            val wheelCX = viewW * 0.31f
                            val wheelCY = viewH * 0.83f
                            val wRad = viewW * 0.16f
                            rotate(degrees = viewModel.steerInput.toFloat() * 64f, pivot = Offset(wheelCX, wheelCY)) {
                                drawCircle(color = Color(0xFF27272A), radius = wRad, center = Offset(wheelCX, wheelCY), style = Stroke(width = 16f))
                                drawLine(color = Color(0xFF27272A), start = Offset(wheelCX, wheelCY), end = Offset(wheelCX - wRad, wheelCY), strokeWidth = 12f)
                                drawLine(color = Color(0xFF27272A), start = Offset(wheelCX, wheelCY), end = Offset(wheelCX + wRad, wheelCY), strokeWidth = 12f)
                                drawLine(color = Color(0xFF27272A), start = Offset(wheelCX, wheelCY), end = Offset(wheelCX, wheelCY + wRad), strokeWidth = 16f)
                                drawCircle(color = Color(0xFF1E293B), radius = 24f, center = Offset(wheelCX, wheelCY))
                                drawCircle(color = Color(0xFF1D4ED8), radius = 10f, center = Offset(wheelCX, wheelCY))
                                drawCircle(color = Color.White, radius = 4f, center = Offset(wheelCX, wheelCY))
                            }
                        }
                    }
                }
            } else {
                // --- CLASSIC MODE (2D TOP DOOR PERSPECTIVE) ---
                val viewW = size.width
                val viewH = size.height
                val cx = viewW / 2
                val cy = viewH / 2

                val px = state.playerX.toFloat()
                val py = state.playerY.toFloat()
                val isWinter = carConfig.targetWeather == "SNOW"

                // Translate camera viewport relative to player 
                drawContext.canvas.save()
                drawContext.transform.translate(cx - px, cy - py)

                // A. Draw green lawn grass field base based on winter or overcast climate
                if (isWinter) {
                    drawRect(
                        color = Color(0xFFD8DBE2),
                        topLeft = Offset(0f, 0f),
                        size = Size(viewModel.mapSize.toFloat(), viewModel.mapSize.toFloat())
                    )
                    for (spot in 250..3750 step 500) {
                        drawCircle(
                            color = Color(0xFF8B8880),
                            radius = 25f,
                            center = Offset(spot.toFloat(), (spot + 120) % 3600f)
                        )
                    }
                } else {
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
                                drawRoundRect(
                                    color = Color(0xFF475569),
                                    topLeft = Offset(obs.rect.left, obs.rect.top),
                                    size = Size(obs.rect.width, obs.rect.height),
                                    cornerRadius = CornerRadius(10f, 10f)
                                )
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
                            ObstacleType.TREE -> {
                                val tx = (obs.rect.left + obs.rect.right) / 2f
                                val ty = (obs.rect.top + obs.rect.bottom) / 2f
                                // Draw double layered green circle cluster
                                drawCircle(
                                    color = Color(0xFF14532D),
                                    radius = 18f,
                                    center = Offset(tx, ty)
                                )
                                drawCircle(
                                    color = Color(0xFF22C55E),
                                    radius = 11f,
                                    center = Offset(tx, ty)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 3f,
                                    center = Offset(tx, ty)
                                )
                            }
                            else -> {}
                        }
                    }
                }

                // E. Render pickups
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
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(cxCar - 9f, cyCar - 12f),
                            size = Size(18f, 24f)
                        )
                        drawRect(
                            color = Color(0xFF0F172A),
                            topLeft = Offset(cxCar + 2f, cyCar - 10f),
                            size = Size(4f, 20f)
                        )

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
                    pivot = Offset(px.toFloat(), py.toFloat())
                ) {
                    if (carConfig.neonUnderglow) {
                        drawCircle(
                            color = Color(0xFF22C55E).copy(alpha = 0.35f),
                            radius = 35f,
                            center = Offset(px.toFloat(), py.toFloat())
                        )
                    }

                    val conePath = Path().apply {
                        moveTo(px.toFloat() + 20f, py.toFloat())
                        lineTo(px.toFloat() + 230f, py.toFloat() - 80f)
                        lineTo(px.toFloat() + 230f, py.toFloat() + 80f)
                        close()
                    }
                    drawPath(
                        path = conePath,
                        color = Color(0xFFFFD700).copy(alpha = 0.14f)
                    )

                    drawRect(Color.Black, topLeft = Offset(px.toFloat() - 16f, py.toFloat() - 18f), size = Size(10f, 5f))
                    drawRect(Color.Black, topLeft = Offset(px.toFloat() + 10f, py.toFloat() - 18f), size = Size(10f, 5f))
                    drawRect(Color.Black, topLeft = Offset(px.toFloat() - 16f, py.toFloat() + 13f), size = Size(10f, 5f))
                    drawRect(Color.Black, topLeft = Offset(px.toFloat() + 10f, py.toFloat() + 13f), size = Size(10f, 5f))

                    drawRoundRect(
                        color = Color(paintCarColor),
                        topLeft = Offset(px.toFloat() - 22f, py.toFloat() - 14f),
                        size = Size(44f, 28f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )

                    val glassPath = Path().apply {
                        moveTo(px.toFloat() - 8f, py.toFloat() - 10f)
                        lineTo(px.toFloat() + 12f, py.toFloat() - 10f)
                        lineTo(px.toFloat() + 18f, py.toFloat())
                        lineTo(px.toFloat() + 12f, py.toFloat() + 10f)
                        lineTo(px.toFloat() - 8f, py.toFloat() + 10f)
                        close()
                    }
                    drawPath(glassPath, Color(0xFF0F172A))

                    if (carConfig.bigSpoiler) {
                        drawLine(
                            color = Color.White,
                            start = Offset(px.toFloat() - 25f, py.toFloat() - 16f),
                            end = Offset(px.toFloat() - 25f, py.toFloat() + 16f),
                            strokeWidth = 5f
                        )
                    }

                    if (state.playerSpeed > 0.1 && viewModel.nitroActive && state.nitroAmount > 0.0) {
                        val flameLength = 30f + (System.currentTimeMillis() % 40)
                        val flamePath = Path().apply {
                            moveTo(px.toFloat() - 22f, py.toFloat() - 6f)
                            lineTo(px.toFloat() - 22f - flameLength, py.toFloat())
                            lineTo(px.toFloat() - 22f, py.toFloat() + 6f)
                            close()
                        }
                        drawPath(flamePath, Color(0xFF00D2FF))
                    }
                }

                // RESTORE Camera
                drawContext.canvas.restore()

                // --- REAL-TIME GRAPHICAL WEATHER OVERLAYS ---
                if (carConfig.targetWeather == "RAIN") {
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
                    drawCircle(
                        color = Color(0x2BFEF08A),
                        radius = 80f,
                        center = Offset(viewW - 100f, 100f)
                    )
                }
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
                // Back to menu & Camera selector row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                    Button(
                        onClick = {
                            cameraViewMode = (cameraViewMode + 1) % 3
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (cameraViewMode) {
                                0 -> Color(0xFF10B981).copy(alpha = 0.15f)
                                1 -> Color(0xFF3B82F6).copy(alpha = 0.15f)
                                else -> Color(0xFFEAB308).copy(alpha = 0.15f)
                            }
                        ),
                        border = BorderStroke(
                            1.dp,
                            when (cameraViewMode) {
                                0 -> Color(0xFF10B981)
                                1 -> Color(0xFF3B82F6)
                                else -> Color(0xFFEAB308)
                            }
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        pText(
                            text = when (cameraViewMode) {
                                0 -> "ВИД: ПОЗАДИ (3D) 🚗"
                                1 -> "ВИД: САЛОН (3D) 💺"
                                else -> "ВИД: КАРТА (2D) 🗺️"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = when (cameraViewMode) {
                                0 -> Color(0xFF10B981)
                                1 -> Color(0xFF3B82F6)
                                else -> Color(0xFFEAB308)
                            }
                        )
                    }
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
