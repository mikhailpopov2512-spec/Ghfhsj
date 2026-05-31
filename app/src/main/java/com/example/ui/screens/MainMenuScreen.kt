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

    var showAdminEntranceDialog by remember { mutableStateOf(false) }
    var adminEntranceUser by remember { mutableStateOf("mikha_q") }
    var adminEntrancePass by remember { mutableStateOf("") }

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
        // Floating Admin Corner Access padlock button (top-right corner entry)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1E293B).copy(alpha = 0.82f))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                .clickable { showAdminEntranceDialog = true }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Admin Entry",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "АДМИН",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF10B981),
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }
        }

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
                        text = "СЕССИЯ: АКТИВНА [ПОЛЬЗОВАТЕЛЬ: mikha_q]",
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
            containerColor = Color(0xFF0F172A),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.border(1.dp, Color(0xFF10B981), RoundedCornerShape(12.dp)),
            title = {
                Text(
                    text = "🔑 ВХОД АДМИНИСТРАТОРА",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Введи имя администратора («mikha_q») и секретный шифр безопасности ДПС.",
                        fontSize = 11.sp,
                        color = Color(0xFFA0A0AB)
                    )

                    // Nickname field
                    OutlinedTextField(
                        value = adminEntranceUser,
                        onValueChange = { adminEntranceUser = it },
                        label = { Text("Имя Пользователя", fontSize = 11.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF020617),
                            unfocusedContainerColor = Color(0xFF020617)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Password field
                    OutlinedTextField(
                        value = adminEntrancePass,
                        onValueChange = { adminEntrancePass = it },
                        label = { Text("Пароль", fontSize = 11.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF020617),
                            unfocusedContainerColor = Color(0xFF020617)
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Password hint instruction
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x1F22C55E))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "💡 ПОДСКАЗКА ПАРОЛЯ:\nШифр безопасности: mikha_pass_2026",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4ADE80),
                            fontFamily = FontFamily.Monospace
                        )
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
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ПОДТВЕРДИТЬ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminEntranceDialog = false }) {
                    Text("ОТМЕНА", color = Color.Gray)
                }
            }
        )
    }
}
