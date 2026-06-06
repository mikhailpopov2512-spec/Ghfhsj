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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
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

    val context = LocalContext.current
    var showRulesDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070A13))
    ) {
        // Full bleed realistic garage main background
        Image(
            painter = painterResource(id = R.drawable.img_launcher_mainbg_1780741320540),
            contentDescription = "Black Russia Garage Splash Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Premium Darkened Vignette Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.45f),
                            Color.Black.copy(alpha = 0.65f)
                        )
                    )
                )
        )

        // 1. TOP LEFT CORNER: Nickname editor / display & Rules informational red button
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 20.dp, top = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Elegant thin-outlined italic/slanted nickname editor box (matches reference screenshot exactly)
            Box(
                modifier = Modifier
                    .width(185.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x33000000))
                    .border(
                        BorderStroke(1.2.dp, Color.White),
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = tempNickname,
                    onValueChange = {
                        tempNickname = it
                        viewModel?.updateNickname(it)
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(Color.White),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Red Info Square Button (matches 'i' exactly!)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFEF4444), Color(0xFFC2410C))
                        )
                    )
                    .border(BorderStroke(1.2.dp, Color.White.copy(alpha = 0.5f)), RoundedCornerShape(6.dp))
                    .clickable { showRulesDialog = true }
                    .testTag("info_rules_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "i", // lower case italic i
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }

        // 2. PROFILE & SERVER ONLINE STATISTIC BAR (Sleek Glassmorphic Floating Panel)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 20.dp, top = 76.dp)
                .width(285.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xD90A0E1A)) // Premium dark slate glass
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Pulsing green dot
                    val pulseScaleAnim by rememberInfiniteTransition(label = "pulse").animateFloat(
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
                            .size(7.dp)
                            .graphicsLayer(scaleX = pulseScaleAnim, scaleY = pulseScaleAnim)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF10B981))
                    )
                    Text(
                        text = "СЕРВЕР 1: СИБИРЬ [ONLINE]",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "843 / 1000",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981),
                    fontFamily = FontFamily.Monospace
                )
            }

            // Fake online progress bar
            LinearProgressIndicator(
                progress = { 0.843f },
                color = Color(0xFF3B82F6),
                trackColor = Color(0xFF1E293B),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)

            // Balance cash & Level row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "БАЛАНС ОПЕРАТОРА",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        fontFamily = FontFamily.Monospace
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(text = "₽", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Black)
                        Text(
                            text = String.format(" %,d", carConfig.cash),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "КРИМИНАЛЬНЫЙ РАНГ",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Ур. ${carConfig.level} | ${currentRank.title.uppercase()}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFE2E8F0),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // 3. TOP RIGHT / EDGE: Secure entry to DPS / ADMIN dashboard
        val isAdminSelected = carConfig.nickname == "mikha_q"
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 20.dp, end = 20.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isAdminSelected) {
                        Brush.linearGradient(colors = listOf(Color(0xFFDC2626), Color(0xFFD97706)))
                    } else {
                        Brush.linearGradient(colors = listOf(Color(0xFF1E293B).copy(alpha = 0.85f), Color(0xFF0F172A).copy(alpha = 0.85f)))
                    }
                )
                .border(
                    width = 1.dp,
                    color = if (isAdminSelected) Color(0xFFFACC15) else Color(0xFF475569),
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable {
                    if (isAdminSelected) {
                        showAdminDialog = true
                    } else {
                        showAdminEntranceDialog = true
                    }
                }
                .padding(horizontal = 12.dp, vertical = 7.dp),
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
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = if (isAdminSelected) "👑 АДМИН-ЦЕНТР" else "🛡️ ВХОД ДПС",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // 4. RIGHT EDGE COLUMN: Social stack (Telegram, Discord, VK)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Telegram Button
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xE61E293B))
                    .border(BorderStroke(1.2.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(10.dp))
                    .clickable {
                        Toast.makeText(context, "Подключение к Telegram сообществу Сибирь!", Toast.LENGTH_SHORT).show()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Send, // paper airplane style
                    contentDescription = "Telegram Community",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Discord Button
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xE61E293B))
                    .border(BorderStroke(1.2.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(10.dp))
                    .clickable {
                        Toast.makeText(context, "Подключение к Discord голосовой рации!", Toast.LENGTH_SHORT).show()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Face, // matches gamer vibe
                    contentDescription = "Discord Voice",
                    tint = Color(0xFF818CF8),
                    modifier = Modifier.size(24.dp)
                )
            }

            // VK Button (Provides ₽10,000 cash for first connection!)
            var vkBonusClaimed by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xE61E293B))
                    .border(BorderStroke(1.2.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(10.dp))
                    .clickable {
                        if (!vkBonusClaimed) {
                            vkBonusClaimed = true
                            viewModel?.adminInjectCustomCash(10000)
                            Toast.makeText(context, "🎉 Бонус за подписку ВК получен: +10,000₽!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Вы уже подписаны на группу VK!", Toast.LENGTH_SHORT).show()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Highly faithful style representing custom VK
                Text(
                    text = "VK",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF60A5FA),
                    fontFamily = FontFamily.SansSerif
                )
            }
        }

        // 5. BOTTOM LEFT PILLS: Settings, Tuning Garage, Donat shop, and Achievements
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "Настройки" Settings Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xE60F172A))
                    .border(BorderStroke(1.2.dp, Color.White.copy(alpha = 0.2f)), RoundedCornerShape(14.dp))
                    .clickable { onNavigateToSettings() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("settings_screen_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Настройки",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // "Гараж" Garage tuning
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xE60F172A))
                    .border(BorderStroke(1.2.dp, Color(0xFF3B82F6).copy(alpha = 0.4f)), RoundedCornerShape(14.dp))
                    .clickable { onNavigateToGarage() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Build,
                    contentDescription = "Garage tuning",
                    tint = Color(0xFF60A5FA),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Гараж",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // "Донат" Shop
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xE60F172A))
                    .border(BorderStroke(1.2.dp, Color(0xFF10B981).copy(alpha = 0.4f)), RoundedCornerShape(14.dp))
                    .clickable { showDonatShop = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ShoppingCart,
                    contentDescription = "Shop",
                    tint = Color(0xFF34D399),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Донат Шоп",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // "Репутация" Stats
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xE60F172A))
                    .border(BorderStroke(1.2.dp, Color(0xFFEAB308).copy(alpha = 0.4f)), RoundedCornerShape(14.dp))
                    .clickable { onNavigateToStats() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Stats",
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Репутация",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 6. BOTTOM CENTER BRANDING: Black Russia Logo
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = "Black",
                    color = Color(0xFFFF4500), // Brand Orange-Red
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Russia",
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
            }
            Text(
                text = "СИБИРСКИЙ КРИМИНАЛ",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 8.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        // 7. BOTTOM RIGHT CORNER: High-Contrast Crimson Gradient "ИГРАТЬ" Button (exact match!)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .width(185.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF881337), Color(0xFF4C0519)) // Deep rich maroon gradient
                    )
                )
                .border(BorderStroke(1.5.dp, Color.White), RoundedCornerShape(8.dp))
                .clickable { onStartGame() }
                .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                .testTag("play_game_button"),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Start game drive",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "ИГРАТЬ",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }
        }

        // 8. VERY BOTTOM LEFT EDGE: Build label (exact match!)
        Text(
            text = "Build 16.21.0",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 22.dp, bottom = 4.dp)
        )
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
