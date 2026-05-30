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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Vibrant Slate base canvas
            .drawBehind {
                val sizeVal = size
                val paintBrush = Brush.linearGradient(
                    colors = listOf(Color(0x1B0284C7), Color(0x3B0F172A)),
                    start = Offset(0f, 0f),
                    end = Offset(sizeVal.width, sizeVal.height)
                )
                drawRect(paintBrush)
            }
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // 1. Slavic RPG Player Profile Info and Rank
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = BorderStroke(1.dp, Color(0xFF384252)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                // Profile Badge Icon
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF0284C7))
                                        .border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Person,
                                        contentDescription = "Profile",
                                        tint = Color.White
                                    )
                                }

                                Column {
                                    if (isEditingName) {
                                        TextField(
                                            value = tempNickname,
                                            onValueChange = { tempNickname = it },
                                            singleLine = true,
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color(0xFF0F172A),
                                                unfocusedContainerColor = Color(0xFF0F172A),
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            ),
                                            modifier = Modifier.width(140.dp)
                                        )
                                    } else {
                                        Text(
                                            text = carConfig.nickname,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }

                                    Text(
                                        text = "Уровень: ${carConfig.level}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF38BDF8),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Edit profile name button
                            IconButton(
                                onClick = {
                                    if (isEditingName) {
                                        viewModel?.updateNickname(tempNickname)
                                        isEditingName = false
                                    } else {
                                        isEditingName = true
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isEditingName) Icons.Filled.Check else Icons.Filled.Edit,
                                    contentDescription = "Change Nickname",
                                    tint = Color(0xFFF1F5F9)
                                )
                            }
                        }

                        // Experience Progress gauge
                        val expProgress = carConfig.experience.toFloat() / 100f
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Опыт: ${carConfig.experience}/100 XP",
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = "Криминальный Авторитет",
                                    fontSize = 10.sp,
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            LinearProgressIndicator(
                                progress = { expProgress },
                                color = Color(0xFF0284C7),
                                trackColor = Color(0xFF0F172A),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }

                        // Authority Rank Badge
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "РАНГ АВТОРИТЕТА",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF94A3B8),
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = currentRank.title.uppercase(),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFFEF4444) // Gritty red accent
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(30.dp))
                                        .background(Color(0x22EF4444))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "СИЛА ГРУППЫ",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEF4444)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Beautiful Russian 2024 Theme Title & Logo Image Placeholder
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Text(
                        text = "РОССИЯ 2024: НА ОКОЛИЦАХ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFEF4444),
                        letterSpacing = 5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "АРХИВЫ ГОПНИКОВ",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Тюнинг Lada Samara, Оффлайн Погони от ДПС, Пушки и Семья",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 3. Persistent Cash and PB Scores Card (Styled in Russian Rubles)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "ОБЩИЙ БАЛАНС (КЭШ)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            letterSpacing = 1.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "₽",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF22C55E) // Ruble green
                            )
                            Text(
                                text = String.format("%,d", carConfig.cash),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "ЛУЧШИЙ СЧЕТ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEAB308)
                        )
                        Text(
                            text = bestScore.toString(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "$totalRuns выездов",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // 4. Primary Drive / Mission Trigger (Pulsing sports design)
            item {
                Button(
                    onClick = onStartGame,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444) // Crimson Red for urgency
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .testTag("play_game_button")
                        .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale),
                    elevation = ButtonDefaults.buttonElevation(10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Drive",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "НАЧАТЬ ОФФЛАЙН ПОГОНЮ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // 5. Customized Lada Garage & Paint Screen Selector
            item {
                Button(
                    onClick = onNavigateToGarage,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("garage_screen_button")
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Build,
                            contentDescription = "Tuning",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ВАЗ ТЮНИНГ-ПРО (ГАРАЖ)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // 6. Donat & Microtransactions Shop Trigger Button (есть донат!)
            item {
                Button(
                    onClick = { showDonatShop = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(1.dp, Color(0xFF22C55E), RoundedCornerShape(12.dp)) // Glowing green donat rim
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = "Donat Shop",
                            tint = Color(0xFF22C55E),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ДОНАТ МАГАЗИН (ОРУЖИЕ, СЕМЬЯ)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // 7. Driver history records and reputation
            item {
                Button(
                    onClick = onNavigateToStats,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E293B)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "History",
                            tint = Color(0xFFEAB308),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ДЕЛА И АЧИВКИ (РЕПУТАЦИЯ)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // 8. Trigger floating Admin panel drawer specifically for "mikha_q" (АДМИН-ПАНЕЛЬ)
            if (carConfig.nickname == "mikha_q") {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF7F1D1D)),
                        border = BorderStroke(2.dp, Color(0xFFF87171)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAdminDialog = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = "Admin powers",
                                    tint = Color.White
                                )
                                Column {
                                    Text(
                                        text = "АДМИН-ПАНЕЛЬ ОБНАРУЖЕНА",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFFECDD3)
                                    )
                                    Text(
                                        text = "Доступ разрешен для: ${carConfig.nickname}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Text(
                                text = "ОТКРЫТЬ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Quick instruction rules card
            item {
                Spacer(modifier = Modifier.height(15.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "КАК ВЫЖИТЬ В СИБИРИ",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = Color(0xFFEAB308)
                        )
                        Text("• Погода сильно влияет на шины! СНЕГ делает ВАЗ неуправляемым саням. ДОЖДЬ заставляет Ляду совать боком.", color = Color.White, fontSize = 12.sp, lineHeight = 16.sp)
                        Text("• Прокачайте Оружие в Донат Меню (Макаров, АК-74у) чтобы стрелять по преследователям из тачки!", color = Color.White, fontSize = 12.sp, lineHeight = 16.sp)
                        Text("• Привлекайте Семью (Братва). Каждый боец дает пассивные выплаты ₽ каждую секунду!", color = Color.White, fontSize = 12.sp, lineHeight = 16.sp)
                        Text("• Ваш криминальный ранг Авторитета растет вместе с Уровнем. Стремитесь стать Паханом!", color = Color.White, fontSize = 12.sp, lineHeight = 16.sp)
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
    // MASSIVE ROBUST ADMIN PANEL DIALOG (mikha_q ONLY)
    // ==========================================
    if (showAdminDialog && carConfig.nickname == "mikha_q") {
        AlertDialog(
            onDismissRequest = { showAdminDialog = false },
            containerColor = Color(0xFF111827), // Gritty slate grey
            title = {
                Text(
                    text = "АДМИН-ПАНЕЛЬ (mikha_q)",
                    fontWeight = FontWeight.Black,
                    color = Color.Red,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Row 1: Give Money + Give EXP
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel?.adminGiveMoney() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+10 млн ₽", color = Color(0xFF22C55E), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel?.adminGiveExperience() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+1000 опыта", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Row 2: Immortality GodMode + Give Full Tuning
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val godModeLabel = if (carConfig.godMode) "Бессмертие ON" else "Бессмертие OFF"
                            Button(
                                onClick = { viewModel?.adminToggleGodMode() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (carConfig.godMode) Color(0xFF22C55E) else Color(0xFFEF4444)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(godModeLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel?.adminFullTuning() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Полный тюниг", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Row 3: Rank promotions & demotions (Понизить / Повысить ранг)
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel?.adminDemoteRank() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Понизить ранг", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { viewModel?.adminSetMaxRank() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("МАКС ранг!", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Row 4: Custom Weather selections (Редактировать погоду)
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937)),
                            border = BorderStroke(1.dp, Color(0xFF4B5563))
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Регулировка Погоды (Реальное Время):",
                                    fontSize = 11.sp,
                                    color = Color(0xFF9CA3AF),
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("OVERCAST", "RAIN", "SNOW").forEach { weatherPreset ->
                                        val isActive = carConfig.targetWeather == weatherPreset
                                        Button(
                                            onClick = { viewModel?.adminSetWeather(weatherPreset) },
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

                    // Row 5: Teleport Matrix triggers
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937)),
                            border = BorderStroke(1.dp, Color(0xFF4B5563))
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Прямой Телепорт на Координаты:",
                                    fontSize = 11.sp,
                                    color = Color(0xFF9CA3AF),
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel?.adminTeleport(1) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                                        modifier = Modifier.weight(1.3f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Тюнинг-Про", fontSize = 9.sp)
                                    }
                                    Button(
                                        onClick = { viewModel?.adminTeleport(2) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                                        modifier = Modifier.weight(1f).height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Раён Банд", fontSize = 9.sp)
                                    }
                                    Button(
                                        onClick = { viewModel?.adminTeleport(3) },
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

                    // Row 6: Spawn cops & Dispatch All cops
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel?.adminSpawnPolicePattern() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9D174D)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Спавн врагов", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel?.adminKillAllPolice() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF065F46)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Убить всех врагов", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Simulated Cheat save integrations
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showCheatSaveMessage = "Состояние сохранено! Чит-сейв записан в локальную БД." },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, Color(0xFF4B5563)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Сохранить игру", fontSize = 10.sp)
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
                                modifier = Modifier.weight(1.1f)
                            ) {
                                Text("Загрузить чит-сейв", fontSize = 10.sp)
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("ЗАКРЫТЬ", color = Color.White)
                }
            }
        )
    }
}
