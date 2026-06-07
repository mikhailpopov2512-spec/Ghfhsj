package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.GameViewModel
import com.example.ui.Screen

@Composable
fun MainMenuScreen(
    viewModel: GameViewModel,
    onNavigateToGarage: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onStartGame: () -> Unit
) {
    val context = LocalContext.current
    val stats by viewModel.playerStats.collectAsState()

    var tempNickname by remember { mutableStateOf(stats.nickname) }
    LaunchedEffect(stats.nickname) {
        tempNickname = stats.nickname
    }

    var showRulesDialog by remember { mutableStateOf(false) }
    var showDonatShop by remember { mutableStateOf(false) }
    var showAdminEntranceDialog by remember { mutableStateOf(false) }
    var showAdminDialog by remember { mutableStateOf(false) }
    var adminEntranceUser by remember { mutableStateOf("mikha_q") }
    var adminEntrancePass by remember { mutableStateOf("") }

    // Pulsing animation for primary big PLAY button
    val infiniteTransition = rememberInfiniteTransition(label = "btnPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "playPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF03050C))
    ) {
        // High fidelity glossy liquid glass background image
        Image(
            painter = painterResource(id = R.drawable.img_launcher_glassbg),
            contentDescription = "Liquid Glass Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Ultra high glossy dark vignette layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.15f),
                            Color(0x8803050C),
                            Color(0xDD03050C)
                        )
                    )
                )
        )

        // Top Left Corner Header - Italic slanted nickname field & Rules info button
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 22.dp, top = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Slanted Editor for Operator Nickname
            Box(
                modifier = Modifier
                    .width(190.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x55000000))
                    .border(
                        BorderStroke(1.2.dp, Color(0x9900F0FF)),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = tempNickname,
                    onValueChange = {
                        tempNickname = it
                        viewModel.updateNickname(it)
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(Color(0xFF00F0FF)),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Info Rules Red Button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFFF007F), Color(0xFFFF0055))
                        )
                    )
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
                    .clickable { showRulesDialog = true }
                    .testTag("info_rules_button"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "i",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic
                )
            }
        }

        // Top Right Corner - Security Entry to Admin center
        val isAdminSelected = stats.nickname == "mikha_q"
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 20.dp, end = 22.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isAdminSelected) {
                        Brush.linearGradient(colors = listOf(Color(0xFF00F0FF), Color(0xFFFF007F)))
                    } else {
                        Brush.linearGradient(colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
                    }
                )
                .border(
                    width = 1.dp,
                    color = if (isAdminSelected) Color.White else Color(0xFF475569),
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
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isAdminSelected) Icons.Filled.Star else Icons.Filled.Lock,
                    contentDescription = "Admin Area",
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = if (isAdminSelected) "👑 АДМИН-ЦЕНТР" else "🛡️ ВХОД ДПС",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Profile & Live Statistics Panel (Liquid Glass Floating Panel)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 22.dp, top = 76.dp)
                .width(290.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0x990A0F1D))
                .border(
                    BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    RoundedCornerShape(16.dp)
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Pulsing live green server dot
                    val pulseScaleAnim by rememberInfiniteTransition("online").animateFloat(
                        initialValue = 0.8f,
                        targetValue = 1.3f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(700, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "dotPulse"
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .graphicsLayer(scaleX = pulseScaleAnim, scaleY = pulseScaleAnim)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF00FF66))
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
                    text = "891 / 1000",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00FF66),
                    fontFamily = FontFamily.Monospace
                )
            }

            LinearProgressIndicator(
                progress = { 0.891f },
                color = Color(0xFF00F0FF),
                trackColor = Color(0x331E293B),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

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
                        color = Color(0xFF94A3B8),
                        fontFamily = FontFamily.Monospace
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "₽",
                            color = Color(0xFF00FF66),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = String.format(" %,d", stats.cash),
                            fontSize = 15.sp,
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
                        color = Color(0xFF94A3B8),
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Ур. ${stats.level} | ${stats.rankTitle.uppercase()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF007F),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Right Edge - Social Buttons Column
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x991E293B))
                    .border(BorderStroke(1.2.dp, Color(0xFF00F0FF).copy(alpha = 0.4f)), RoundedCornerShape(12.dp))
                    .clickable {
                        Toast.makeText(context, "Подключение к Telegram сообществу Сибирь!", Toast.LENGTH_SHORT).show()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Telegram Community",
                    tint = Color(0xFF00F0FF),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Discord Button
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x991E293B))
                    .border(BorderStroke(1.2.dp, Color(0xFFFF007F).copy(alpha = 0.4f)), RoundedCornerShape(12.dp))
                    .clickable {
                        Toast.makeText(context, "Подключение к Discord голосовому каналу!", Toast.LENGTH_SHORT).show()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Headphones,
                    contentDescription = "Discord channel",
                    tint = Color(0xFFFF007F),
                    modifier = Modifier.size(22.dp)
                )
            }

            // VK Button (pays ₽15,000 cash for first connection!)
            var vkBonusClaimed by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x991E293B))
                    .border(BorderStroke(1.2.dp, Color(0xFF00FF66).copy(alpha = 0.4f)), RoundedCornerShape(12.dp))
                    .clickable {
                        if (!vkBonusClaimed) {
                            vkBonusClaimed = true
                            viewModel.adminInjectCustomCash(15000)
                            Toast.makeText(context, "🎉 Бонус за подписку VK получен: +15,000₽!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Вы уже подписаны на группу VK!", Toast.LENGTH_SHORT).show()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "VK",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }

        // Center Content - Interactive Pulse Game Launch Button
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 55.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val currentMap by viewModel.selectedMapIndex.collectAsState()
            
            Text(
                text = "⚡ МАРШРУТ СИБИРСКОГО ТЮНИНГА ⚡",
                color = Color(0xFF00F0FF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.5.sp
            )
            
            Row(
                modifier = Modifier
                    .width(320.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x77050914))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)), RoundedCornerShape(16.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Snowy Irtysh
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (currentMap == 0) Color(0x3300F0FF) else Color.Transparent)
                        .border(
                            width = 1.2.dp,
                            color = if (currentMap == 0) Color(0xFF00F0FF) else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { viewModel.selectMap(0) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("❄️", fontSize = 14.sp)
                        Text(
                            text = "ИРТЫШ",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "ДРИФТ x1.5",
                            color = Color(0xFF00F0FF),
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Baikal Highway
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (currentMap == 1) Color(0x22F59E0B) else Color.Transparent)
                        .border(
                            width = 1.2.dp,
                            color = if (currentMap == 1) Color(0xFFF59E0B) else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { viewModel.selectMap(1) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌲", fontSize = 14.sp)
                        Text(
                            text = "БАЙКАЛ",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "СКОРОСТЬ",
                            color = Color(0xFFF59E0B),
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Night Novosibirsk
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (currentMap == 2) Color(0x33FF007F) else Color.Transparent)
                        .border(
                            width = 1.2.dp,
                            color = if (currentMap == 2) Color(0xFFFF007F) else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { viewModel.selectMap(2) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌃", fontSize = 14.sp)
                        Text(
                            text = "НГС КОРТ",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "МОНЕТЫ x2",
                            color = Color(0xFFFF007F),
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onStartGame,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .width(320.dp)
                    .height(60.dp)
                    .testTag("play_game_button")
                    .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFF007F), Color(0xFFFF5500))
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .border(BorderStroke(1.5.dp, Color.White), RoundedCornerShape(20.dp)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
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
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Text(
                text = "СИБИРЬ ДРИФТ • ВЕРСИЯ 2.0.0",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        // Bottom Bar - Redesign Liquid Glass Tab buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 22.dp, bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "Настройки" Settings Button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xAA0F172A))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(14.dp))
                    .clickable { onNavigateToSettings() }
                    .padding(horizontal = 14.dp, vertical = 11.dp)
                    .testTag("settings_screen_button"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Настройки",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // "Гараж" Tuning
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xAA0F172A))
                    .border(BorderStroke(1.dp, Color(0xFF00F0FF).copy(alpha = 0.3f)), RoundedCornerShape(14.dp))
                    .clickable { onNavigateToGarage() }
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Build,
                    contentDescription = "Garage tuning",
                    tint = Color(0xFF00F0FF),
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Тюнинг",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // "Донат Шоп"
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xAA0F172A))
                    .border(BorderStroke(1.dp, Color(0xFFFF007F).copy(alpha = 0.3f)), RoundedCornerShape(14.dp))
                    .clickable { showDonatShop = true }
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ShoppingCart,
                    contentDescription = "Shop",
                    tint = Color(0xFFFF007F),
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Донат Шоп",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // "Репутация" Stats
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xAA0F172A))
                    .border(BorderStroke(1.dp, Color(0xFFFF9F00).copy(alpha = 0.3f)), RoundedCornerShape(14.dp))
                    .clickable { onNavigateToStats() }
                    .padding(horizontal = 14.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Rank",
                    tint = Color(0xFFFF9F00),
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Репутация",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Dialogue popup: Game Rules (Slanted elegant theme)
        if (showRulesDialog) {
            AlertDialog(
                onDismissRequest = { showRulesDialog = false },
                containerColor = Color(0xFB0A0F1D),
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = "📜 ЗАКОНЫ СИБИРСКОГО ДРИФТА",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "1. Скорость превыше всего — маневрируйте на полной скорости, уходя от патрулей ДПС.",
                            color = Color(0xFFC3D1E6),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "2. Собирайте монеты дрифта на асфальте, чтобы покупать форсированные двигатели и неоновую подсветку.",
                            color = Color(0xFFC3D1E6),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "3. Повышайте ранги — от Новичка до авторитетного Пахана вашей криминальной банды.",
                            color = Color(0xFFC3D1E6),
                            fontSize = 11.sp
                        )
                        Text(
                            text = "4. VIP Секрет: Смените ник на 'mikha_q', чтобы войти под системным доступом к Спеццентру ДПС!",
                            color = Color(0xFF00F0FF),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showRulesDialog = false }) {
                        Text("ПОНЯЛ, СИБИРЯК", color = Color(0xFFFF007F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.border(BorderStroke(1.2.dp, Color(0xFFFF007F)), RoundedCornerShape(20.dp))
            )
        }

        // Dialogue popup: Donat shop
        if (showDonatShop) {
            AlertDialog(
                onDismissRequest = { showDonatShop = false },
                containerColor = Color(0xFB0A0F1D),
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = "💎 ДОНАТ ШОП СИБИРИ",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Купите рубиновые моменты пополнения или донат-кейсы Сибири!",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x33FFFFFF))
                                .clickable {
                                    viewModel.adminInjectCustomCash(50000)
                                    Toast.makeText(context, "💎 Начислено +50,000₽ по донату!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💸 СИБИРСКИЙ КЕЙС (+50k ₽)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("БЕСПЛАТНО", color = Color(0xFF00FF66), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x33FFFFFF))
                                .clickable {
                                    viewModel.adminInjectCustomCash(250000)
                                    Toast.makeText(context, "💎 Начислено +250,000₽ по донату!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("👑 ОЛИГАРХ ПАКЕТ (+250k ₽)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("БЕСПЛАТНО", color = Color(0xFF00FF66), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showDonatShop = false }) {
                        Text("ЗАКРЫТЬ", color = Color.White, fontSize = 11.sp)
                    }
                },
                modifier = Modifier.border(BorderStroke(1.2.dp, Color(0xFF00F0FF)), RoundedCornerShape(20.dp))
            )
        }

        // Dialogue popup: Admin password entry for non-authorized name
        if (showAdminEntranceDialog) {
            AlertDialog(
                onDismissRequest = { showAdminEntranceDialog = false },
                containerColor = Color(0xFB0A0F1D),
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = "🛡️ ВХОД В ДПС СИБИРИ",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Введите логин и пароль Силовых Ведомств или смените никнейм на 'mikha_q'",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )

                        OutlinedTextField(
                            value = adminEntranceUser,
                            onValueChange = { adminEntranceUser = it },
                            label = { Text("Оператор-Логин", color = Color.White.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00F0FF),
                                unfocusedBorderColor = Color(0xFF475569)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = adminEntrancePass,
                            onValueChange = { adminEntrancePass = it },
                            label = { Text("Пароль доступа", color = Color.White.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00F0FF),
                                unfocusedBorderColor = Color(0xFF475569)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(onClick = { showAdminEntranceDialog = false }) {
                            Text("ОТМЕНА", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                if (adminEntranceUser == "mikha_q" || adminEntrancePass == "12345") {
                                    viewModel.updateNickname("mikha_q")
                                    showAdminEntranceDialog = false
                                    showAdminDialog = true
                                    Toast.makeText(context, "Вход ДПС выполнен успешно!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Ошибка доступа ДПС!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF))
                        ) {
                            Text("ВОЙТИ", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                modifier = Modifier.border(BorderStroke(1.2.dp, Color(0xFF00F0FF).copy(alpha = 0.5f)), RoundedCornerShape(20.dp))
            )
        }

        // Dialogue popup: Full Authorized Admin console (if nickname is mikha_q)
        if (showAdminDialog) {
            AlertDialog(
                onDismissRequest = { showAdminDialog = false },
                containerColor = Color(0xFB05070B),
                shape = RoundedCornerShape(20.dp),
                title = {
                    Text(
                        text = "👑 КОНСОЛЬ АДМИНА mikha_q",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Быстрые операции ДПС Модификатора:",
                            color = Color(0xFFFF9F00),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = {
                                viewModel.adminInjectCustomCash(500000)
                                Toast.makeText(context, "Начислено 500,000₽!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D172A)),
                            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF00FF66), RoundedCornerShape(8.dp))
                        ) {
                            Text("💰 Добавить +500,000₽", color = Color(0xFF00FF66), fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.applyMaxUpgrades()
                                Toast.makeText(context, "Тюнинг вашей Лады форсирован до 5 уровня!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D172A)),
                            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF00F0FF), RoundedCornerShape(8.dp))
                        ) {
                            Text("🔧 Максимальный тюнинг Лады", color = Color(0xFF00F0FF), fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.resetStats()
                                Toast.makeText(context, "Статистика сброшена до исходной!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1120)),
                            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFFF007F), RoundedCornerShape(8.dp))
                        ) {
                            Text("❌ Сбросить профиль", color = Color(0xFFFF007F), fontSize = 11.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showAdminDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text("ЗАКРЫТЬ", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.border(BorderStroke(1.2.dp, Color(0xFFFF9F00)), RoundedCornerShape(20.dp))
            )
        }
    }
}
