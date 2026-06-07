package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.GameViewModel

@Composable
fun StatsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val stats by viewModel.playerStats.collectAsState()

    // Gauge calculation for Operators rank progression
    val xpProgress = stats.experience.toFloat() / 100f

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

        // Backdrop tint overlays
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xCD03050C))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x33FFFFFF))
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)), RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "🏅 РЕПУТАЦИЯ И РАНГИ",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )

                // Placeholder to balance header
                Spacer(modifier = Modifier.width(44.dp))
            }

            // Stats Glass panels
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x660A0F1D))
                    .border(BorderStroke(1.2.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(20.dp))
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Rank Overview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x3300F0FF))
                            .border(BorderStroke(1.2.dp, Color(0xFF00F0FF)), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Crown Title",
                            tint = Color(0xFF00F0FF),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column {
                        Text(
                            text = stats.nickname.uppercase(),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "РАНГ АВТОРИТЕТА: ${stats.rankTitle.uppercase()}",
                            color = Color(0xFFFF007F),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                // XP progress display
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "ПРОГРЕСС ДО СЛЕДУЮЩЕГО УРОВНЯ",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8),
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${stats.experience} / 100 XP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00F0FF),
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    LinearProgressIndicator(
                        progress = { xpProgress },
                        color = Color(0xFF00F0FF),
                        trackColor = Color(0x331E293B),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                // Performance Scorecards grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ScoreStatCard(
                        title = "МАКС. ДРИФТ-СЧЕТ",
                        value = "${stats.highscore} pt",
                        icon = Icons.Filled.TrendingUp,
                        themeColor = Color(0xFF00FF66),
                        modifier = Modifier.weight(1f)
                    )

                    ScoreStatCard(
                        title = "ВСЕГО ВЫЕЗДОВ",
                        value = "${stats.totalRuns} выездов",
                        icon = Icons.Filled.CardMembership,
                        themeColor = Color(0xFFFF007F),
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                // Criminal Milestones info list
                Text(
                    text = "ЗАДНИ КРИМИНАЛЬНЫЕ ДОСТИЖЕНИЯ",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AchievementItem(
                        title = "🚀 ПЕРВЫЕ ДЕНЬГИ ДРИФТА",
                        description = "Получите любой выигрыш баланса уходя от патрулей ДПС.",
                        isUnlocked = stats.totalRuns > 0
                    )
                    AchievementItem(
                        title = "🔧 УЛИЧНЫЙ ПРОКАЧИК",
                        description = "Установите тюнинг двигателя Lada до 3-го уровня или выше.",
                        isUnlocked = stats.engineLevel >= 3
                    )
                    AchievementItem(
                        title = "⚡ СИБИРСКИЙ ШЕРИФ",
                        description = "Достигните ранга Бригадир (уровень 7) или выше.",
                        isUnlocked = stats.level >= 7
                    )
                }
            }
        }
    }
}

@Composable
fun ScoreStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    themeColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x33FFFFFF))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = themeColor,
                modifier = Modifier.size(16.dp)
            )
        }

        Text(
            text = value,
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun AchievementItem(
    title: String,
    description: String,
    isUnlocked: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isUnlocked) Color(0x1A00FF66) else Color(0x1AFFFFFF))
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = if (isUnlocked) Color(0x3300FF66) else Color.White.copy(alpha = 0.1f)
                ),
                RoundedCornerShape(14.dp)
            )
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isUnlocked) Color(0x3300FF66) else Color(0x1AFFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isUnlocked) "✔️" else "🔒",
                fontSize = 14.sp
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp
            )
        }
    }
}

