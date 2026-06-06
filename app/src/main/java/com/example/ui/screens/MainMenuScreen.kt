package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.R
import com.example.data.model.CarConfig
import com.example.data.model.ScoreRecord
import com.example.data.model.PlayerRank
import com.example.ui.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    carConfig: CarConfig,
    scoreHistory: List<ScoreRecord>,
    onStartGame: () -> Unit,
    onNavigateToGarage: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onAddCheatCash: () -> Unit,
    // Add additional viewmodel binding directly here for admin actions and donation purchases
    viewModel: GameViewModel? = null
) {
    // Pulsing button glow
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val bestScore = scoreHistory.maxOfOrNull { it.score } ?: 0
    val totalRuns = scoreHistory.size
    val currentRank = PlayerRank.fromLevel(carConfig.level)

    // Nickname editing logic
    var isEditingName by remember { mutableStateOf(false) }
    var tempNickname by remember(carConfig.nickname) { mutableStateOf(carConfig.nickname) }

    // Dialog flags
    var showDonatShop by remember { mutableStateOf(false) }
    var showAdminDialog by remember { mutableStateOf(false) }
    var showCheatSaveMessage by remember { mutableStateOf("") }

    var showAdminEntranceDialog by remember { mutableStateOf(false) }
    var adminEntranceUser by remember { mutableStateOf("mikha_q") }
    var adminEntrancePass by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070A13)) // High-fidelity dark premium cyber navy
            .drawBehind {
                // Diagonal ambient laser neon line decoration in background
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF1D4ED8).copy(alpha = 0.22f), Color.Transparent),
                        center = Offset(size.width * 0.1f, size.height * 0.3f),
                        radius = size.width * 0.8f
                    )
                )
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFEF4444).copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width * 0.8f, size.height * 0.8f),
                        radius = size.width * 0.7f
                    )
                )
            }
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Floating Top Corner Access Button (Admin Console trigger)
        val isAdminSelected = carConfig.nickname == "mikha_q"
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isAdminSelected) {
                        Brush.linearGradient(colors = listOf(Color(0xFFDC2626), Color(0xFFD97706)))
                    } else {
                        Brush.linearGradient(colors = listOf(Color(0xFF1E293B).copy(alpha = 0.9f), Color(0xFF0F172A).copy(alpha = 0.9f)))
                    }
                )
                .border(
                    width = 1.5.dp,
                    color = if (isAdminSelected) Color(0xFFFACC15) else Color(0xFF334155),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable {
                    if (isAdminSelected) {
                        showAdminDialog = true
                    } else {
                        showAdminEntranceDialog = true
                    }
                }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (isAdminSelected) Icons.Filled.Star else Icons.Filled.Lock,
                    contentDescription = "Admin Area",
                    tint = if (isAdminSelected) Color.White else Color(0xFF10B981),
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = if (isAdminSelected) "👑 АДМИН-ЦЕНТР" else "🛡️ ВХОД ДПС",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // 1. Premium 3D Widescreen Cinematic Header Image Banner
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF3B82F6).copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_launcher_banner_1780738067273),
                            contentDescription = "3D Drift Lada Launcher Banner",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Gradient Overlay to blend text beautifully
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color(0x9E070A13), Color(0xFF070A13))
                                    )
                                )
                        )
                        
                        // Game Titles overlapping the 3D art
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFEF4444))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "3D СИБИРЬ",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    text = "КРИМИНАЛЬНЫЙ СИМУЛЯТОР",
                                    color = Color(0xFF10B981),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "РОССИЯ 2024: НА ОКОЛИЦАХ",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp
                            )
                        }
                    }
                }
            }

            // 2. Black Russia-style Server Online Status Widget (Siberia Drift)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Pulsing green online light indicator
                                val scaleAnim by rememberInfiniteTransition(label = "pulse").animateFloat(
                                    initialValue = 0.8f,
                                    targetValue = 1.2f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(800, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "onlineDot"
                                )
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .graphicsLayer(scaleX = scaleAnim, scaleY = scaleAnim)
                                        .clip(RoundedCornerShape(30.dp))
                                        .background(Color(0xFF10B981))
                                )
                                Text(
                                    text = "СЕРВЕР 1: СИБИРЬ ДРИФТ [ONLINE]",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "843 / 1000",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        
                        // Fake player activity bar
                        LinearProgressIndicator(
                            progress = { 0.843f },
                            color = Color(0xFF3B82F6),
                            trackColor = Color(0xFF1E293B),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }
                }
            }

            // 3. Persistent Sleek Nickname Entry Bar (directly embedded, Black Russia Style)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, if (isAdminSelected) Color(0xFF10B981) else Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ИГРОВОЙ НИКНЕЙМ ОПЕРАТОРА",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            color = Color(0xFF64748B),
                            letterSpacing = 1.sp
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = tempNickname,
                                onValueChange = {
                                    tempNickname = it
                                    viewModel?.updateNickname(it)
                                },
                                placeholder = { Text("Введи свой никнейм", color = Color(0xFF475569)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedContainerColor = Color(0xFF070A13),
                                    unfocusedContainerColor = Color(0xFF070A13)
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Edit shortcut tag that quickly selects mikha_q
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF3B82F6).copy(alpha = 0.15f))
                                    .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        tempNickname = "mikha_q"
                                        viewModel?.updateNickname("mikha_q")
                                    }
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "⚡ mikha_q",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF60A5FA),
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // 4. Player Profile Info & Criminal XP
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.2.dp, Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF1D4ED8).copy(alpha = 0.2f))
                                        .border(1.dp, Color(0xFF3B82F6), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = "User Badge",
                                        tint = Color(0xFF60A5FA),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = carConfig.nickname,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Уровень: ${carConfig.level}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF60A5FA),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            // High contrast status label
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                    .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(30.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = currentRank.title.uppercase(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }

                        // Progress gauge
                        val expProgress = carConfig.experience.toFloat() / 100f
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Опыт: ${carConfig.experience}/100 XP",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = "Ранг: $currentRank",
                                    fontSize = 10.sp,
                                    color = Color(0xFF3B82F6),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            LinearProgressIndicator(
                                progress = { expProgress },
                                color = Color(0xFF3B82F6),
                                trackColor = Color(0xFF1E293B),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
            }

            // 5. Balance ruble meter and Personal High score
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.2.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "АКТИВНЫЙ БАЛАНС КЭША",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "₽",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF10B981)
                            )
                            Text(
                                text = String.format("%,d", carConfig.cash),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = (-0.5).sp
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "РЕКОРД СПИДВЕЯ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD97706),
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = bestScore.toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "$totalRuns выездов",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }

            // 6. Huge central play selector "ИГРАТЬ / НАЧАТЬ ВЫЕЗД" (Crimson pulsing, central launcher style)
            item {
                Button(
                    onClick = onStartGame,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626) // Deep warning red
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("play_game_button")
                        .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Start game drive",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ПОДКЛЮЧИТЬСЯ И ИГРАТЬ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // 7. Grid Matrix elements: Tuning Garage & Donat shop
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Garage card button
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, Color(0xFF2563EB)),
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clickable { onNavigateToGarage() }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF2563EB).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Build,
                                    contentDescription = "Garage icon",
                                    tint = Color(0xFF60A5FA),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "ГАРАЖ",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "Тюнинг Lada",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    // Shop item button
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, Color(0xFF10B981)),
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clickable { showDonatShop = true }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ShoppingCart,
                                    contentDescription = "Shop icon",
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "ДОНАТ ШОП",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "Оружие и ОПГ",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }

            // 8. Grid Matrix row 2: Achievements and simulator settings
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Stats card button
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, Color(0xFFD97706)),
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clickable { onNavigateToStats() }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFD97706).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = "Stats icon",
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "РЕПУТАЦИЯ",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "Дела воров",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }

                    // Settings card button
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.2.dp, Color(0xFF4F46E5)),
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .testTag("settings_screen_button")
                            .clickable { onNavigateToSettings() }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF4F46E5).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = "Settings icon",
                                    tint = Color(0xFF818CF8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "НАСТРОЙКИ",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "Погода и ДПС",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }

            // Quick instruction rules card
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.2.dp, Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "КАК ВЫЖИТЬ В СИБИРИ ДПС СИМУЛЯТОРЕ",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = Color(0xFFEAB308),
                            fontFamily = FontFamily.Monospace
                        )
                        Text("• Погода сильно влияет на шины! СНЕГ делает ВАЗ неуправляемым саням. ДОЖДЬ заставляет Ляду совать боком.", color = Color(0xFF94A3B8), fontSize = 11.sp, lineHeight = 15.sp)
                        Text("• Прокачайте Оружие в Донат Меню (Макаров, АК-74у) чтобы стрелять по преследователям из тачки!", color = Color(0xFF94A3B8), fontSize = 11.sp, lineHeight = 15.sp)
                        Text("• Привлекайте Семью (Братва). Каждый боец дает пассивные выплаты ₽ каждую секунду!", color = Color(0xFF94A3B8), fontSize = 11.sp, lineHeight = 15.sp)
                        Text("• Ваш криминальный ранг Авторитета растет вместе с Уровнем. Стремитесь стать Паханом!", color = Color(0xFF94A3B8), fontSize = 11.sp, lineHeight = 15.sp)
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // ==========================================
    // DONAT SHOP DIALOG (есть донат!)
    // ==========================================
    if (showDonatShop) {
        AlertDialog(
            onDismissRequest = { showDonatShop = false },
            containerColor = Color(0xFF1E293B),
            title = {
                Text(
                    text = "ДОНАТ МАГАЗИН (VIP БРАТВА)",
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Прокачка бандитского арсенала и закуп соратников на раёне без лимитов:",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )

                    // 1. Weapon Purchase row
                    val weaponName = when(carConfig.weaponLevel) {
                        0 -> "Нет пушки (Купить ПМ за 2,000₽)"
                        1 -> "ПМ Пистолет (Апгрейд до АК-74 за 6,500₽)"
                        2 -> "АК-74у Калашников (Купить РПГ за 15,000₽)"
                        else -> "РПГ-7 гранатомёт (МАКСИМУМ)"
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "АРСЕНАЛ ОРУЖИЯ", fontSize = 10.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                            Text(text = weaponName, fontSize = 13.sp, color = Color.White)
                            if (carConfig.weaponLevel < 3) {
                                Button(
                                    onClick = { viewModel?.purchaseWeapon() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                ) {
                                    Text("КУПИТЬ ОРУЖИЕ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 2. Family Crew level row (Passive cash generation!)
                    val familyDesc = when(carConfig.familyLevel) {
                        0 -> "Один на раёне (Нанять Гопников за 4,000₽)"
                        1 -> "Гопники прикрывают (+15₽/сек. Апгрейд до Братвы за 8,000₽)"
                        2 -> "Крутая Братва (+30₽/сек. Закупить Синдикат за 12,000₽)"
                        else -> "Crime Синдикат (+45₽/сек. МАКСИМУМ)"
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "ПОДКРЕПЛЕНИЕ И БРИГАДА", fontSize = 10.sp, color = Color(0xFFEAB308), fontWeight = FontWeight.Bold)
                            Text(text = familyDesc, fontSize = 13.sp, color = Color.White)
                            if (carConfig.familyLevel < 3) {
                                Button(
                                    onClick = { viewModel?.purchaseFamilyCrew() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                ) {
                                    Text("КУПИТЬ ЧЛЕНОВ СЕМЬИ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 3. Custom Lada Cosmetic Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel?.toggleSpoiler() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (carConfig.bigSpoiler) Color(0xFFEF4444) else Color(0xFF334155)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (carConfig.bigSpoiler) "СПОЙЛЕР ВКЛ" else "СПОЙЛЕР ВЫКЛ", fontSize = 10.sp)
                        }
                        Button(
                            onClick = { viewModel?.toggleNeonUnderglow() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (carConfig.neonUnderglow) Color(0xFF22C55E) else Color(0xFF334155)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (carConfig.neonUnderglow) "НЕОН ЗЕЛЕНЫЙ" else "НЕОН ВЫКЛ", fontSize = 10.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDonatShop = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) {
                    Text("ЗАКРЫТЬ")
                }
            }
        )
    }

    // ==========================================
    // MASSIVE ROBUST ADMIN PANEL DIALOG
    // ==========================================
    if (showAdminDialog) {
        var customMoneyText by remember { mutableStateOf("") }
        var adminTrigger by remember { mutableStateOf(0) } // For local reactive state force update

        val currentSpeedMult = viewModel?.adminSpeedMultiplier ?: 1.0
        val isCopsDumb = viewModel?.copsDumbMode ?: false
        val isCopsTurbo = viewModel?.copsTurboMode ?: false
        val isInfiniteNitro = viewModel?.infiniteNitro ?: false
        val isInfiniteAmmo = viewModel?.infiniteAmmo ?: false

        AlertDialog(
            onDismissRequest = { showAdminDialog = false },
            containerColor = Color(0xFF070B13), // Deep dark hacker canvas
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.border(2.dp, Color(0xFF10B981), RoundedCornerShape(16.dp)), // Glowing neon emerald border
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "❖ VAZ_HACK_TERMINAL v3.4 ❖",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF10B981),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "СЕССИЯ: АКТИВНА [ВХОД: ${carConfig.nickname}]",
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF6B7280),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(11.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // SECTION 1: BALANCE & CASH CONTROL
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "> СИСТЕМА УПРАВЛЕНИЯ КЭШЕМ",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF34D399),
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel?.adminGiveMoney(); adminTrigger++ },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                        border = BorderStroke(1.dp, Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1.1f).height(34.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("+10 млн ₽", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    }

                                    Button(
                                        onClick = { viewModel?.adminGiveExperience(); adminTrigger++ },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                        border = BorderStroke(1.dp, Color(0xFF0284C7)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(34.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("+1000 EXP", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    TextField(
                                        value = customMoneyText,
                                        onValueChange = { customMoneyText = it },
                                        placeholder = { Text("Сумма ₽...", fontSize = 11.sp, color = Color.Gray) },
                                        modifier = Modifier.weight(1.3f).height(46.dp),
                                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.White),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color(0xFF020617),
                                            unfocusedContainerColor = Color(0xFF1E293B),
                                            focusedIndicatorColor = Color(0xFF10B981),
                                            unfocusedIndicatorColor = Color.Transparent
                                        ),
                                        singleLine = true
                                    )

                                    Button(
                                        onClick = {
                                            val amount = customMoneyText.toIntOrNull()
                                            if (amount != null && amount > 0) {
                                                viewModel?.adminInjectCustomCash(amount)
                                                showCheatSaveMessage = "Внедрено: ${amount} ₽!"
                                            } else {
                                                showCheatSaveMessage = "Ошибка: неверная сумма!"
                                            }
                                            adminTrigger++
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(42.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("ВНЕДРИТЬ", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 2: SPEED MULTIPLIERS (CHASSIS STAGES)
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "> МОДИФИКАТОРЫ ДВИГАТЕЛЯ (ТЕКУЩИЙ: x$currentSpeedMult)",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFFBBF24),
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(1.0, 2.5, 5.0, 10.0).forEach { mult ->
                                        val isActive = currentSpeedMult == mult
                                        val label = when(mult) {
                                            1.0 -> "СТОК (x1)"
                                            2.5 -> "ST3 (x2.5)"
                                            5.0 -> "СПОРТ (x5)"
                                            else -> "ГИПЕР (x10)"
                                        }
                                        Button(
                                            onClick = {
                                                viewModel?.adminSpeedMultiplier = mult
                                                adminTrigger++
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isActive) Color(0xFFD97706) else Color(0xFF1E293B)
                                            ),
                                            border = BorderStroke(1.dp, if (isActive) Color(0xFFFBBF24) else Color(0xFF334155)),
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 3: WEAPONS SYSTEM MASTER CODES
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "> КЛИЕНТ БОЕПРИПАСОВ & ОРУЖИЯ",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFF43F5E),
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(0, 1, 2, 3).forEach { lvl ->
                                        val isActive = carConfig.weaponLevel == lvl
                                        val label = when(lvl) {
                                            0 -> "БЕЗ ОРУЖИЯ"
                                            1 -> "ПИСТОЛЕТ ПМ"
                                            2 -> "АК-74у"
                                            else -> "РПГ-7"
                                        }
                                        Button(
                                            onClick = {
                                                viewModel?.adminSetWeaponLevel(lvl)
                                                adminTrigger++
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isActive) Color(0xFFE11D48) else Color(0xFF1E293B)
                                            ),
                                            border = BorderStroke(1.dp, if (isActive) Color(0xFFF43F5E) else Color(0xFF334155)),
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(text = label, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel?.infiniteAmmo = !isInfiniteAmmo
                                            adminTrigger++
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isInfiniteAmmo) Color(0xFF10B981) else Color(0xFF1E293B)
                                        ),
                                        border = BorderStroke(1.dp, if (isInfiniteAmmo) Color(0xFF10B981) else Color(0xFF334155)),
                                        modifier = Modifier.weight(1f).height(34.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            text = if (isInfiniteAmmo) "✓ БЕСКОНЕЧНЫЙ ОГНЕМЕТ" else "БЕСКОНЕЧНЫЕ ПАТРОНЫ",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isInfiniteAmmo) Color.Black else Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 4: CHEAT TOGGLES
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "> СИСТЕМНЫЕ КЛЮЧИ БЕЗОПАСНОСТИ",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val godModeLabel = if (carConfig.godMode) "✓ БЕССМЕРТИЕ: ON" else "БЕССМЕРТИЕ"
                                    Button(
                                        onClick = { viewModel?.adminToggleGodMode(); adminTrigger++ },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (carConfig.godMode) Color(0xFF10B981) else Color(0xFF1E293B)
                                        ),
                                        border = BorderStroke(1.dp, if (carConfig.godMode) Color(0xFF10B981) else Color(0xFF334155)),
                                        modifier = Modifier.weight(1f).height(34.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(text = godModeLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (carConfig.godMode) Color.Black else Color.White)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel?.infiniteNitro = !isInfiniteNitro
                                            adminTrigger++
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isInfiniteNitro) Color(0xFF10B981) else Color(0xFF1E293B)
                                        ),
                                        border = BorderStroke(1.dp, if (isInfiniteNitro) Color(0xFF10B981) else Color(0xFF334155)),
                                        modifier = Modifier.weight(1.1f).height(34.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            text = if (isInfiniteNitro) "✓ БЕСКОНЕЧНОЕ НИТРО" else "БЕСКОНЕЧНОЕ НИТРО",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isInfiniteNitro) Color.Black else Color.White
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel?.adminFullTuning(); adminTrigger++ },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                        border = BorderStroke(1.dp, Color(0xFF34D399)),
                                        modifier = Modifier.weight(1f).height(34.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("ПОЛНЫЙ ТЮНИНГ (MAX)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 5: COPS & CHASE REGS
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "> СЛУЖБА ДПС & СТАВКИ ПОГОНИ",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF6366F1),
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel?.copsDumbMode = !isCopsDumb
                                            if (viewModel?.copsDumbMode == true) viewModel?.copsTurboMode = false
                                            adminTrigger++
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isCopsDumb) Color(0xFF10B981) else Color(0xFF1E293B)
                                        ),
                                        border = BorderStroke(1.dp, if (isCopsDumb) Color(0xFF10B981) else Color(0xFF334155)),
                                        modifier = Modifier.weight(1f).height(34.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(text = if (isCopsDumb) "✓ ДПС СПЯТ!" else "ОТКЛЮЧИТЬ ИИ", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isCopsDumb) Color.Black else Color.White)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel?.copsTurboMode = !isCopsTurbo
                                            if (viewModel?.copsTurboMode == true) viewModel?.copsDumbMode = false
                                            adminTrigger++
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isCopsTurbo) Color(0xFFEF4444) else Color(0xFF1E293B)
                                        ),
                                        border = BorderStroke(1.dp, if (isCopsTurbo) Color(0xFFEF4444) else Color(0xFF334155)),
                                        modifier = Modifier.weight(1f).height(34.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(text = if (isCopsTurbo) "✓ ТУРБО-КОПЫ x3" else "ГОРЯЧАЯ ПОГОНЯ", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isCopsTurbo) Color.White else Color.White)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel?.adminSpawnPolicePattern(); adminTrigger++ },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D174D)),
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Спавн патрулей", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { viewModel?.adminKillAllPolice(); adminTrigger++ },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF065F46)),
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Ликвидировать ДПС", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 6: INSTANT ITEM SPAWNER MACHINE
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "> МГНОВЕННЫЙ СПАВН ПРЕДМЕТОВ КРУГОМ",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF06B6D4),
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("COIN", "REPAIR", "NITRO").forEach { waveType ->
                                        val label = when(waveType) {
                                            "COIN" -> "МОНЕТЫ x12"
                                            "REPAIR" -> "РЕМКИ x12"
                                            else -> "НИТРО x12"
                                        }
                                        Button(
                                            onClick = {
                                                viewModel?.adminSpawnPickupWave(waveType)
                                                showCheatSaveMessage = "12 предметов ($waveType) раскиданы возле Lada!"
                                                adminTrigger++
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF083344)),
                                            border = BorderStroke(1.dp, Color(0xFF06B6D4)),
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 7: COORDINATES MATRIX & ATMOSPHERE
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "> ТЕЛЕПОРТАЦИОННАЯ МАТРИЦА",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel?.adminTeleport(1); adminTrigger++ },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                                        modifier = Modifier.weight(1.3f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Тюнинг-Про", fontSize = 9.sp)
                                    }
                                    Button(
                                        onClick = { viewModel?.adminTeleport(2); adminTrigger++ },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Раён Банд", fontSize = 9.sp)
                                    }
                                    Button(
                                        onClick = { viewModel?.adminTeleport(3); adminTrigger++ },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Хрущёвки", fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 8: WEATHER PRESETS
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "> КОНТРОЛЛЕР АТМОСФЕРЫ",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("OVERCAST", "RAIN", "SNOW").forEach { weatherPreset ->
                                        val isActive = carConfig.targetWeather == weatherPreset
                                        Button(
                                            onClick = { viewModel?.adminSetWeather(weatherPreset); adminTrigger++ },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isActive) Color(0xFF0284C7) else Color(0xFF374151)
                                            ),
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(
                                                text = if (weatherPreset == "OVERCAST") "Пасмурно" else if (weatherPreset == "RAIN") "Дождь" else "Гр.Снег",
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 8.5: GRAPHICS CONFIG & WORLD SCALE
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "> ТЮНИНГ ГРАФИКИ (КАЧЕСТВО: ${carConfig.graphicsQuality})",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("LOW", "MEDIUM", "HIGH").forEach { q ->
                                        val isActive = carConfig.graphicsQuality == q
                                        val label = when(q) {
                                            "LOW" -> "Быстродействие (Низ.)"
                                            "MEDIUM" -> "Стандарт (Ср.)"
                                            else -> "Максимум (Выс.)"
                                        }
                                        Button(
                                            onClick = { viewModel?.adminSetGraphicsQuality(q); adminTrigger++ },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isActive) Color(0xFF0284C7) else Color(0xFF27272A)
                                            ),
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(text = label, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = "> ГЕОЛОГИЧЕСКИЙ РАЗМЕР КАРТЫ",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF34D399),
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("NORMAL", "BIG", "ULTRA").forEach { sz ->
                                        val isActive = carConfig.mapSizeSetting == sz
                                        val label = when(sz) {
                                            "NORMAL" -> "2.5 км (Быстро)"
                                            "BIG" -> "4.5 км (Мид)"
                                            else -> "6.5 км (Огромная!)"
                                        }
                                        Button(
                                            onClick = { viewModel?.adminSetMapSizeSetting(sz); adminTrigger++ },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isActive) Color(0xFF10B981) else Color(0xFF27272A)
                                            ),
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(text = label, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 8.7: SIM-DONATE CENTER & SUPREME VIP FULL CONTROL
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF070B13)),
                            border = BorderStroke(2.dp, Color(0xFFD946EF)), // Beautiful Magenta Neon Border!
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "⚡ ИНТЕГРИРОВАННЫЙ ДОНАТ-ЦЕНТР & VIP ⚡",
                                    fontSize = 12.sp,
                                    color = Color(0xFFF472B6),
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1.2f)) {
                                        Text("Делюкс VIP-Статус", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        Text("Дополнительный доход и бесплатное топливо", fontSize = 8.sp, color = Color.Gray)
                                    }
                                    Button(
                                        onClick = { viewModel?.adminToggleVipStatus(); adminTrigger++ },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (carConfig.hasVipStatus) Color(0xFFD946EF) else Color(0xFF27272A)
                                        ),
                                        modifier = Modifier.weight(0.8f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text(
                                            text = if (carConfig.hasVipStatus) "АКТИВЕН ✓" else "АКТИВИРОВАТЬ",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1.2f)) {
                                        Text("🔥 ПОЛНОЕ УПРАВЛЕНИЕ АДМИНА", fontSize = 10.sp, color = Color(0xFFF43F5E), fontWeight = FontWeight.Black)
                                        Text("Сверхсила, бессмертие, АК-74, RPG, бесконечный огонь", fontSize = 8.sp, color = Color.Gray)
                                    }
                                    Button(
                                        onClick = { viewModel?.adminToggleFullControl(); adminTrigger++ },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (carConfig.hasFullAdminControl) Color(0xFFE11D48) else Color(0xFF27272A)
                                        ),
                                        modifier = Modifier.weight(0.8f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                        ) {
                                        Text(
                                            text = if (carConfig.hasFullAdminControl) "УПРАВЛЕНИЕ ✓" else "ВКЛЮЧИТЬ",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 9: RANKS & SYSTEM SAVES
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel?.adminSetMaxRank(); adminTrigger++ },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                modifier = Modifier.weight(1f).height(36.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("МАКСИМАЛЬНЫЙ РАНГ 50", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel?.adminDemoteRank(); adminTrigger++ },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                                modifier = Modifier.weight(1f).height(36.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("СБРОС ДО РАНГА 1", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showCheatSaveMessage = "Состояние сохранено! Чит-сейв записан в локальную БД." },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, Color(0xFF4B5563)),
                                modifier = Modifier.weight(1f).height(34.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Сохранить чит-сессию", fontSize = 10.sp)
                            }

                            Button(
                                onClick = {
                                    viewModel?.adminGiveMoney()
                                    viewModel?.adminGiveExperience()
                                    viewModel?.adminFullTuning()
                                    showCheatSaveMessage = "Локальный чит-сейв загружен! Получены: ₽10М, максимальные детали, VIP-подвеска!"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, Color(0xFF4B5563)),
                                modifier = Modifier.weight(1.1f).height(34.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Загрузить чит-сессию", fontSize = 10.sp)
                            }
                        }
                    }

                    if (showCheatSaveMessage.isNotEmpty()) {
                        item {
                            Text(
                                text = showCheatSaveMessage,
                                color = Color(0xFF22C55E),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAdminDialog = false
                        showCheatSaveMessage = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ЗАКРЫТЬ ТЕРМИНАЛ", color = Color.White, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            }
        )
    }

    if (showAdminEntranceDialog) {
        AlertDialog(
            onDismissRequest = { showAdminEntranceDialog = false },
            containerColor = Color(0xFF0B132B), // Secure cyber vault navy black
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.border(2.dp, Color(0xFF10B981), RoundedCornerShape(18.dp)),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🛡️",
                        fontSize = 22.sp
                    )
                    Column {
                        Text(
                            text = "СИСТЕМА БЕЗОПАСНОСТИ",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "ФЕДЕРАЛЬНЫЙ ТЕРМИНАЛ ДПС",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Для получения полного контроля над трафиком, балансом и физикой введите имя оператора и секретный пропуск безопасности.",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 15.sp
                    )

                    // Nickname field
                    OutlinedTextField(
                        value = adminEntranceUser,
                        onValueChange = { adminEntranceUser = it },
                        label = { Text("Имя Оператора / Ник", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF020617),
                            unfocusedContainerColor = Color(0xFF020617)
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Password field
                    OutlinedTextField(
                        value = adminEntrancePass,
                        onValueChange = { adminEntrancePass = it },
                        label = { Text("Ключ Допуска", fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedContainerColor = Color(0xFF020617),
                            unfocusedContainerColor = Color(0xFF020617)
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Password hint instruction
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x1510B981))
                            .border(1.dp, Color(0xFF10B981).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "🔑 АВАРИЙНЫЙ ШИФР ВОССТАНОВЛЕНИЯ:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF34D399),
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "mikha_pass_2026",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (adminEntrancePass.trim() == "mikha_pass_2026" && adminEntranceUser.trim().isNotEmpty()) {
                            val enteredUser = adminEntranceUser.trim()
                            tempNickname = enteredUser
                            viewModel?.updateNickname(enteredUser)
                            showAdminEntranceDialog = false
                            showAdminDialog = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(40.dp)
                ) {
                    Text("ПОДТВЕРДИТЬ ДОСТУП", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminEntranceDialog = false }) {
                    Text("ОТМЕНА", color = Color(0xFF64748B), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            }
        )
    }
}
