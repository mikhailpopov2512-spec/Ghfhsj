package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.GameViewModel
import com.example.ui.Screen
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

// Data classes for game simulation elements
data class CopCar(
    var x: Float,
    var y: Float,
    var speedY: Float,
    var color: Color = Color(0xFF1E40AF),
    var isFlasherRed: Boolean = true
)

data class DriftCoin(
    var x: Float,
    var y: Float,
    var isBig: Boolean = false
)

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val stats by viewModel.playerStats.collectAsState()
    val selectedMap by viewModel.selectedMapIndex.collectAsState()

    // Game states
    var score by remember { mutableStateOf(0) }
    var coinsCollected by remember { mutableStateOf(0) }
    var speedKmh by remember { mutableStateOf(80) }
    var nitroFuel by remember { mutableStateOf(100f) }
    var activeShieldHealth by remember { mutableStateOf(100f) }
    var isGameOver by remember { mutableStateOf(false) }

    // Player position coordinates (within virtual 0 to 400 track width)
    var playerX by remember { mutableStateOf(200f) }
    var playerSlideVelocity by remember { mutableStateOf(0f) }
    var isSteeringLeft by remember { mutableStateOf(false) }
    var isSteeringRight by remember { mutableStateOf(false) }
    var isNitroActive by remember { mutableStateOf(false) }

    // Dynamic obstacle elements
    val copCars = remember { mutableStateListOf<CopCar>() }
    val driftCoins = remember { mutableStateListOf<DriftCoin>() }
    var roadOffset by remember { mutableStateOf(0f) }

    // Vibration feedback helper
    fun triggerCollisionVibrate() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(200)
            }
        }
    }

    // Active multipliers from Tuning Garage upgrades
    val engineMultiplier = 1f + (stats.engineLevel - 1) * 0.15f
    val nitroChargeRate = 0.5f + (stats.nitroLevel - 1) * 0.2f
    val shieldDiscount = 1f - (stats.armorLevel - 1) * 0.12f

    // Underglow neon visual color mapping
    val neonColor = when (stats.neonThemeId) {
        1 -> Color(0xFF00F0FF) // Cyber Cyan
        2 -> Color(0xFFFF007F) // Plasma Pink
        3 -> Color(0xFFFF9F00) // Tokyo Gold
        else -> Color.Transparent
    }

    // Initialize/reset game objects
    fun restartGame() {
        score = 0
        coinsCollected = 0
        speedKmh = 80
        nitroFuel = 100f
        activeShieldHealth = 100f
        playerX = 200f
        isGameOver = false
        copCars.clear()
        driftCoins.clear()

        // Spawning initial objects
        copCars.add(CopCar(x = 100f, y = -100f, speedY = 5f))
        copCars.add(CopCar(x = 300f, y = -400f, speedY = 7f))
        driftCoins.add(DriftCoin(x = 150f, y = -200f))
        driftCoins.add(DriftCoin(x = 250f, y = -500f))
    }

    // Main Game Loop LaunchedEffect
    LaunchedEffect(isGameOver) {
        if (!isGameOver) {
            restartGame()
            while (!isGameOver) {
                delay(20) // Game step tick speed: ~50 fps

                // 1. Progress speed calculations
                var targetSpeed = if (isNitroActive && nitroFuel > 0f) {
                    220f * engineMultiplier
                } else {
                    130f * engineMultiplier
                }
                
                // Add minor random speed jitters for raw high-speed feel
                targetSpeed += Random.nextInt(-4, 5)
                
                val speedDiff = targetSpeed - speedKmh
                speedKmh += (speedDiff * 0.08f).toInt()

                // 2. Consume or recharge nitro fuel
                if (isNitroActive && nitroFuel > 0f) {
                    nitroFuel = max(0f, nitroFuel - 1.2f)
                    if (nitroFuel <= 0f) {
                        isNitroActive = false
                    }
                } else {
                    nitroFuel = min(100f, nitroFuel + 0.12f * nitroChargeRate)
                }

                // 3. User steering movement values
                val steerPower = if (selectedMap == 0) 0.8f else 1.5f
                val maxVelocity = if (selectedMap == 0) 11f else 8.5f
                val dragFactor = if (selectedMap == 0) 0.94f else 0.70f
                
                if (isSteeringLeft) {
                    playerSlideVelocity = max(-maxVelocity, playerSlideVelocity - steerPower)
                } else if (isSteeringRight) {
                    playerSlideVelocity = min(maxVelocity, playerSlideVelocity + steerPower)
                } else {
                    playerSlideVelocity *= dragFactor
                }
                
                val windDraft = if (selectedMap == 0) {
                    Random.nextFloat() * 0.4f - 0.2f
                } else {
                    0f
                }
                
                playerX = (playerX + playerSlideVelocity + windDraft).coerceIn(40f, 360f)

                // 4. Update highway elements offset
                roadOffset = (roadOffset + (speedKmh / 10f)) % 300f
                val scoreMult = if (selectedMap == 0) 1.5f else 1.0f
                score += ((speedKmh / 20).coerceAtLeast(1) * scoreMult).toInt()

                // 5. Update spawned obstacle cop cars
                copCars.forEachIndexed { idx, cop ->
                    // Cops drift down based on relative speed
                    val copSpeedFactor = when (selectedMap) {
                        0 -> 0.80f  // Ice slipperiness
                        1 -> 1.35f  // Baikal Highway rapid patrol pursuit
                        else -> 1.0f
                    }
                    cop.y += ((speedKmh / 25f) + cop.speedY) * copSpeedFactor
                    cop.isFlasherRed = !cop.isFlasherRed // Alternate flashing cop lights

                    // ACTIVE INTRUDER BLOCKING PURSUIT AI
                    val copSteerAgility = when (selectedMap) {
                        0 -> 0.012f // Snowy, slippery for the cops too!
                        1 -> 0.045f // Super high performance Interceptors on Baikal! Strong chase!
                        else -> 0.026f // Standard responsive pursuit
                    }
                    if (cop.y < 420f && cop.y > -50f) {
                        val steerDelta = playerX - cop.x
                        cop.x += steerDelta * copSteerAgility
                    }
                    cop.x = cop.x.coerceIn(50f, 350f)

                    // Collision checking (Virtual Player range box vs. Cop box)
                    if (cop.y > 440f && cop.y < 500f && abs(cop.x - playerX) < 42f) {
                        // Impact collision detected!
                        val dmg = 32f * shieldDiscount
                        activeShieldHealth = max(0f, activeShieldHealth - dmg)
                        triggerCollisionVibrate()
                        cop.y = -200f // Reset cop offscreen
                        cop.x = Random.nextInt(60, 340).toFloat()

                        if (activeShieldHealth <= 0f) {
                            isGameOver = true
                        }
                    }

                    // Recycle cop car
                    if (cop.y > 650f) {
                        cop.y = -150f
                        cop.x = Random.nextInt(60, 340).toFloat()
                        cop.speedY = Random.nextFloat() * 5f + 3f
                    }
                }

                // 6. Update drift coins placement
                driftCoins.forEach { coin ->
                    coin.y += (speedKmh / 25f)

                    // Collection checking
                    if (coin.y > 450f && coin.y < 510f && abs(coin.x - playerX) < 36f) {
                        val valMult = if (selectedMap == 2) 2 else 1
                        coinsCollected += (if (coin.isBig) 5 else 1) * valMult
                        coin.y = -200f
                        coin.x = Random.nextInt(60, 340).toFloat()
                    }

                    // Recycle coin
                    if (coin.y > 650f) {
                        coin.y = -Random.nextInt(100, 800).toFloat()
                        coin.x = Random.nextInt(60, 340).toFloat()
                        coin.isBig = Random.nextInt(1, 10) == 1
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF03050C))
    ) {
        // High fidelity glossy vertical background art
        Image(
            painter = painterResource(id = R.drawable.img_launcher_glassbg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark alpha shield tint
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x9903050C))
        )

        // Active speed track Canvas renderer
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val trackW = size.width
            val trackH = size.height

            // Render street shoulder boundaries under different maps
            val roadBgColor = when (selectedMap) {
                0 -> Color(0xFF233245) // Snowy, icy dark slate asphalt
                1 -> Color(0xFF131A26) // Classic midnight blue highway
                else -> Color(0xFF02040A) // Absolute cyber dark court
            }

            drawRect(
                color = roadBgColor,
                size = Size(trackW, trackH)
            )

            // Dynamic highway driving dashed lines depending on route
            val dashH = 50f
            val spaceH = 40f
            val totalLineH = dashH + spaceH
            var lineY = -totalLineH + (roadOffset % totalLineH)

            val boardersColor = when (selectedMap) {
                0 -> Color(0xFF90A4AE) // Snowy frosted steel
                1 -> Color(0xFFE2E8F0) // Safety white shoulder stripes
                else -> Color(0xFFFF007F) // Luminescent hot pink grid bounds
            }

            val centersColor = when (selectedMap) {
                0 -> Color(0xFFE0F7FA) // Winter cold icy white
                1 -> Color(0xFFF59E0B) // Baikal double yellow lane markers
                else -> Color(0xFF00F0FF) // Cyber cyan divider marker
            }

            while (lineY < trackH) {
                // Left Border lines
                drawLine(
                    color = boardersColor,
                    start = Offset(x = 40.dp.toPx(), y = lineY),
                    end = Offset(x = 40.dp.toPx(), y = lineY + dashH),
                    strokeWidth = 3.5.dp.toPx()
                )
                // Center dividing highway markers
                drawLine(
                    color = centersColor,
                    start = Offset(x = trackW / 2, y = lineY),
                    end = Offset(x = trackW / 2, y = lineY + dashH),
                    strokeWidth = (if (selectedMap == 1) 4.dp else 2.dp).toPx()
                )
                // Right border lines
                drawLine(
                    color = boardersColor,
                    start = Offset(x = trackW - 40.dp.toPx(), y = lineY),
                    end = Offset(x = trackW - 40.dp.toPx(), y = lineY + dashH),
                    strokeWidth = 3.5.dp.toPx()
                )
                lineY += totalLineH
            }

            // Map 0: Snowy weather overlay (falling particles drifting deterministically)
            if (selectedMap == 0) {
                for (i in 0 until 24) {
                    val snowSeedX = (i * 24329.81f) % 400f
                    val snowSeedY = ((i * 74902.93f) + (roadOffset * 4.8f)) % 600f
                    
                    val sx = (snowSeedX / 400f) * trackW
                    val sy = (snowSeedY / 600f) * trackH
                    
                    drawCircle(
                        color = Color.White.copy(alpha = 0.75f),
                        radius = (3f + (i % 4)).dp.toPx(),
                        center = Offset(sx, sy)
                    )
                }
            }

            // Draw player spawned coins
            driftCoins.forEach { coin ->
                val virtualRatioX = trackW / 400f
                val scrX = coin.x * virtualRatioX
                val scrY = (coin.y / 600f) * trackH

                drawCircle(
                    color = if (coin.isBig) Color(0xFFFF9F00) else Color(0xFF00FF66),
                    radius = if (coin.isBig) 13.dp.toPx() else 8.dp.toPx(),
                    center = Offset(scrX, scrY)
                )

                drawCircle(
                    color = Color.White,
                    radius = if (coin.isBig) 6.dp.toPx() else 3.dp.toPx(),
                    center = Offset(scrX, scrY)
                )
            }

            // Draw chasing Police Cruiser cars with emergency lights
            copCars.forEach { cop ->
                val virtualRatioX = trackW / 400f
                val cX = cop.x * virtualRatioX
                val cY = (cop.y / 600f) * trackH

                // Sirens light sweep projection fields
                drawLine(
                    color = (if (cop.isFlasherRed) Color.Red else Color.Cyan).copy(alpha = 0.22f),
                    start = Offset(cX - 12.dp.toPx(), cY - 5.dp.toPx()),
                    end = Offset(cX - 100.dp.toPx(), cY - 45.dp.toPx()),
                    strokeWidth = 26.dp.toPx()
                )
                drawLine(
                    color = (if (cop.isFlasherRed) Color.Cyan else Color.Red).copy(alpha = 0.22f),
                    start = Offset(cX + 12.dp.toPx(), cY - 5.dp.toPx()),
                    end = Offset(cX + 100.dp.toPx(), cY - 45.dp.toPx()),
                    strokeWidth = 26.dp.toPx()
                )

                // Detailed Wheels for police cruiser
                drawRect(color = Color.Black, topLeft = Offset(cX - 23.dp.toPx(), cY - 25.dp.toPx()), size = Size(5.dp.toPx(), 12.dp.toPx()))
                drawRect(color = Color.Black, topLeft = Offset(cX + 18.dp.toPx(), cY - 25.dp.toPx()), size = Size(5.dp.toPx(), 12.dp.toPx()))
                drawRect(color = Color.Black, topLeft = Offset(cX - 23.dp.toPx(), cY + 15.dp.toPx()), size = Size(5.dp.toPx(), 12.dp.toPx()))
                drawRect(color = Color.Black, topLeft = Offset(cX + 18.dp.toPx(), cY + 15.dp.toPx()), size = Size(5.dp.toPx(), 12.dp.toPx()))

                // Police sedan chassis with custom push ram bar
                drawRect(
                    color = Color(0xFF0F172A),
                    topLeft = Offset(cX - 20.dp.toPx(), cY - 35.dp.toPx()),
                    size = Size(40.dp.toPx(), 70.dp.toPx())
                )
                // Metal bumper push guard
                drawRect(
                    color = Color(0xFFE2E8F0),
                    topLeft = Offset(cX - 16.dp.toPx(), cY - 38.dp.toPx()),
                    size = Size(32.dp.toPx(), 3.dp.toPx())
                )
                // Windshield window glass
                drawRect(
                    color = Color(0xFF475569),
                    topLeft = Offset(cX - 15.dp.toPx(), cY - 12.dp.toPx()),
                    size = Size(30.dp.toPx(), 18.dp.toPx())
                )
                // White doors decals
                drawRect(
                    color = Color.White,
                    topLeft = Offset(cX - 20.dp.toPx(), cY - 20.dp.toPx()),
                    size = Size(4.dp.toPx(), 45.dp.toPx())
                )
                drawRect(
                    color = Color.White,
                    topLeft = Offset(cX + 16.dp.toPx(), cY - 20.dp.toPx()),
                    size = Size(4.dp.toPx(), 45.dp.toPx())
                )
                // Siren flasher bar
                drawRect(
                    color = if (cop.isFlasherRed) Color.Red else Color.Cyan,
                    topLeft = Offset(cX - 12.dp.toPx(), cY - 5.dp.toPx()),
                    size = Size(10.dp.toPx(), 5.dp.toPx())
                )
                drawRect(
                    color = if (cop.isFlasherRed) Color.Cyan else Color.Red,
                    topLeft = Offset(cX + 2.dp.toPx(), cY - 5.dp.toPx()),
                    size = Size(10.dp.toPx(), 5.dp.toPx())
                )
            }

            // Draw Players Lada Sports car with Underglow
            val virtualRatioX = trackW / 400f
            val pX = playerX * virtualRatioX
            val pY = 0.8f * trackH

            // 1. Underglow neon drawing
            if (neonColor != Color.Transparent) {
                drawCircle(
                    color = neonColor.copy(alpha = 0.6f),
                    radius = 34.dp.toPx(),
                    center = Offset(pX, pY + 12.dp.toPx())
                )
            }

            // Active Headlights active night path beams projection
            drawLine(
                color = Color(0x55FFFDF0),
                start = Offset(pX - 15.dp.toPx(), pY - 38.dp.toPx()),
                end = Offset(pX - 45.dp.toPx(), pY - 160.dp.toPx()),
                strokeWidth = 20.dp.toPx()
            )
            drawLine(
                color = Color(0x55FFFDF0),
                start = Offset(pX + 15.dp.toPx(), pY - 38.dp.toPx()),
                end = Offset(pX + 45.dp.toPx(), pY - 160.dp.toPx()),
                strokeWidth = 20.dp.toPx()
            )

            // Active exhaust sparks/fire on Nitro booster
            if (isNitroActive && nitroFuel > 0f) {
                // Left exhaust flare
                drawLine(
                    color = Color(0xFFFF5500),
                    start = Offset(pX - 12.dp.toPx(), pY + 40.dp.toPx()),
                    end = Offset(pX - 22.dp.toPx(), pY + 65.dp.toPx()),
                    strokeWidth = 6.dp.toPx()
                )
                drawCircle(
                    color = Color(0xFFFFCC00),
                    radius = 8.dp.toPx(),
                    center = Offset(pX - 22.dp.toPx(), pY + 65.dp.toPx())
                )
                // Right exhaust flare
                drawLine(
                    color = Color(0xFFFF5500),
                    start = Offset(pX + 12.dp.toPx(), pY + 40.dp.toPx()),
                    end = Offset(pX + 22.dp.toPx(), pY + 65.dp.toPx()),
                    strokeWidth = 6.dp.toPx()
                )
                drawCircle(
                    color = Color(0xFFFFCC00),
                    radius = 8.dp.toPx(),
                    center = Offset(pX + 22.dp.toPx(), pY + 65.dp.toPx())
                )
            }

            // 4 Rubber detailed wheels
            drawRect(color = Color.Black, topLeft = Offset(pX - 25.dp.toPx(), pY - 28.dp.toPx()), size = Size(5.dp.toPx(), 13.dp.toPx()))
            drawRect(color = Color.Black, topLeft = Offset(pX + 20.dp.toPx(), pY - 28.dp.toPx()), size = Size(5.dp.toPx(), 13.dp.toPx()))
            drawRect(color = Color.Black, topLeft = Offset(pX - 25.dp.toPx(), pY + 18.dp.toPx()), size = Size(5.dp.toPx(), 13.dp.toPx()))
            drawRect(color = Color.Black, topLeft = Offset(pX + 20.dp.toPx(), pY + 18.dp.toPx()), size = Size(5.dp.toPx(), 13.dp.toPx()))

            // 2. Main red/chrome Lada body
            drawRect(
                color = Color(0xFFC21807), // Detailed Siberian Crimson Ruby Red Metallic
                topLeft = Offset(pX - 22.dp.toPx(), pY - 40.dp.toPx()),
                size = Size(44.dp.toPx(), 80.dp.toPx())
            )
            // Carbon Fiber Hood Stripes
            drawRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(pX - 10.dp.toPx(), pY - 40.dp.toPx()),
                size = Size(20.dp.toPx(), 22.dp.toPx())
            )
            // Rear sport wing spoiler
            drawRect(
                color = Color.Black,
                topLeft = Offset(pX - 25.dp.toPx(), pY + 34.dp.toPx()),
                size = Size(50.dp.toPx(), 8.dp.toPx())
            )
            // Windshield and side windows cabin
            drawRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(pX - 16.dp.toPx(), pY - 18.dp.toPx()),
                size = Size(32.dp.toPx(), 28.dp.toPx())
            )
            // Active headlight circles
            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = Offset(pX - 15.dp.toPx(), pY - 38.dp.toPx())
            )
            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = Offset(pX + 15.dp.toPx(), pY - 38.dp.toPx())
            )
        }

        // TOP STATS HUD Overlay (Liquid Glass Style)
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left HUD Card - Drift Score & Coins count
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x990F172A))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "ДРИФТ-СЧЕТ: ${score} pt",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "МОНЕТЫ: +$coinsCollected ₽",
                    color = Color(0xFF00FF66),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Center Speedometer Indicator Circle (Highly high-tech!)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xBB0A0F1D))
                    .border(BorderStroke(2.dp, Color(0xFF00F0FF)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$speedKmh",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "KM/H",
                        color = Color(0xFF00F0FF),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Right HUD: Armor & Nitro Fuel indicators
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x990F172A))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .width(100.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Shield line
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("БРОНЯ КУЗОВА", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(
                        progress = { activeShieldHealth / 100f },
                        color = Color(0xFFFF007F),
                        trackColor = Color(0x33FFFFFF),
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                    )
                }
                // Nitro NOS fuel bar
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("ЗАКИСЬ NITRO", color = Color.White.copy(alpha = 0.6f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(
                        progress = { nitroFuel / 100f },
                        color = Color(0xFF00FF66),
                        trackColor = Color(0x33FFFFFF),
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                    )
                }
            }
        }

        // BOTTOM DRIVING ACTION CONTROLS (Tappable overlay touch buttons)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT COLUMN STEERING BUTTONS
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Turn Left
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color(0x990F172A))
                        .border(BorderStroke(1.5.dp, Color.White.copy(alpha = 0.3f)), CircleShape)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isSteeringLeft = true
                                    tryAwaitRelease()
                                    isSteeringLeft = false
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("◀", color = Color.White, fontSize = 28.sp)
                }

                // Turn Right
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color(0x990F172A))
                        .border(BorderStroke(1.5.dp, Color.White.copy(alpha = 0.3f)), CircleShape)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isSteeringRight = true
                                    tryAwaitRelease()
                                    isSteeringRight = false
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("▶", color = Color.White, fontSize = 28.sp)
                }
            }

            // RIGHT STACK: NITRO GAS PEDAL
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = if (isNitroActive) listOf(Color(0xFFFF007F), Color(0xFFFF5500)) else listOf(Color(0xFF00F0FF), Color(0xFF0D9488))
                        )
                    )
                    .border(BorderStroke(2.dp, Color.White), CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isNitroActive = true
                                tryAwaitRelease()
                                isNitroActive = false
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.FlashOn,
                        contentDescription = "nitro boost pedal",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "NOS",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // GAME OVER SLANTED MODAL OVERLAY
        if (isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xDD000000)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .width(320.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFB0A0F1D))
                        .border(BorderStroke(1.5.dp, Color(0xFFFF007F)), RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        text = "🚨 ЗАДЕРЖАНИЕ СОТР. ДПС",
                        color = Color(0xFFFF007F),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )

                    Text(
                        text = "Ваша Lada Сибирь заблокирована патрульными экипажами. Оформлен протокол дрифта!",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    // Results readout cards inside GameOver
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Очки Дрифта", color = Color.White, fontSize = 12.sp)
                            Text("$score pt", color = Color(0xFF00F0FF), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Выигрыш ₽ (Монеты)", color = Color.White, fontSize = 12.sp)
                            val earnedRuble = coinsCollected * 150
                            Text("+₽${String.format("%,d", earnedRuble)}", color = Color(0xFF00FF66), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Полученный Опыт API", color = Color.White, fontSize = 12.sp)
                            val xpEarned = (score / 150).coerceAtLeast(10)
                            Text("+$xpEarned XP", color = Color(0xFFFF9F00), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Button(
                        onClick = {
                            val earnedRuble = coinsCollected * 150
                            val xpEarned = (score / 150).coerceAtLeast(10)
                            viewModel.saveRunResult(score, earnedRuble, xpEarned)
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Text(
                            text = "ЗАБРАТЬ ВЫРУЧКУ И ВЫЙТИ",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
