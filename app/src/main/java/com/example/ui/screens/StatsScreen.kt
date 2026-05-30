package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScoreRecord
import com.example.ui.GameViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatsScreen(
    viewModel: GameViewModel,
    onBackToMenu: () -> Unit
) {
    val scoreHistory by viewModel.scoreHistoryState.collectAsState()

    // Vibrant Palette Theme Colors
    val slateBG = Color(0xFF0F172A)
    val slateCard = Color(0xFF1E293B)
    val blueAccent = Color(0xFF3B82F6)
    val yellowGold = Color(0xFFFFD700)
    val greenNeon = Color(0xFF10B981)
    val redDanger = Color(0xFFEF4444)

    val totalRuns = scoreHistory.size
    val maxScore = scoreHistory.maxOfOrNull { it.score } ?: 0
    val totalEscapes = scoreHistory.count { it.escaped }
    val maxHeat = scoreHistory.maxOfOrNull { it.heatLevel } ?: 1
    val averageDuration = if (totalRuns > 0) scoreHistory.map { it.durationSeconds }.average().toInt() else 0

    // Calculates driver class rank
    val escapePercent = if (totalRuns > 0) (totalEscapes * 100) / totalRuns else 0
    val driverRank = when {
        totalRuns == 0 -> "СВЕЖИЙ КАДЕТ"
        escapePercent >= 80 && maxScore > 5000 -> "ЛЕГЕНДАРНЫЙ ПАХАН"
        escapePercent >= 65 -> "ОБЪЕЗДЧИК ДПС 1-Й СТЕПЕНИ"
        escapePercent >= 40 -> "БЫВАЛЫЙ ДРИФТЕР"
        else -> "ШОФЕР ШЕСТЕРКА"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(slateBG)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBackToMenu,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(slateCard)
                            .border(1.dp, Color(0xFF334155), CircleShape)
                            .testTag("stats_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    pText(
                        text = "ЛИЧНОЕ ДЕЛО ВОДИТЕЛЯ",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    
                    Spacer(modifier = Modifier.width(48.dp)) // Equalizer spacer
                }
            }

            // Driver profile license widget representing Russian Driver Mafia
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = slateCard),
                    border = BorderStroke(2.dp, yellowGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Driver Badge",
                            tint = yellowGold,
                            modifier = Modifier.size(40.dp)
                        )

                        pText(
                            text = "КРИМИНАЛЬНАЯ ЛИЦЕНЗИЯ СССР",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFA0A0AB),
                            letterSpacing = 2.sp
                        )

                        pText(
                            text = driverRank,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )

                        Row(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(30.dp))
                                .background(redDanger.copy(alpha = 0.2f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            pText(
                                text = "УСПЕШНЫЙ ПОБЕГ: $escapePercent%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = redDanger
                            )
                        }
                    }
                }
            }

            // Summary grid cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DriverStatsCard(
                        title = "ЛИЧНЫЙ РЕКОРД",
                        value = "$maxScore ₽",
                        accentColor = yellowGold,
                        modifier = Modifier.weight(1f)
                    )
                    DriverStatsCard(
                        title = "ВСЕГО ПОГОНЬ",
                        value = totalRuns.toString(),
                        accentColor = blueAccent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DriverStatsCard(
                        title = "МАКС РОЗЫСК",
                        value = "Lvl $maxHeat",
                        accentColor = redDanger,
                        modifier = Modifier.weight(1f)
                    )
                    
                    val formattedDuration = String.format("%02d:%02d", averageDuration / 60, averageDuration % 60)
                    DriverStatsCard(
                        title = "СРЕДНЕЕ ВРЕМЯ",
                        value = formattedDuration,
                        accentColor = greenNeon,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Historical Table logs
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    pText(
                        text = "АРХИВНЫЕ ДЕЛА НА СЕВЕРЕ",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
            }

            if (scoreHistory.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = slateCard),
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        border = BorderStroke(1.dp, Color(0xFF334155))
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            pText(
                                text = "Архив пуст.\nУдирай от преследующих патрулей ДПС, чтобы оставить след в истории!",
                                color = Color(0xFFA0A0AB),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(scoreHistory) { record ->
                    HistoricalLogItem(record)
                }
                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun DriverStatsCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            pText(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFA0A0AB),
                letterSpacing = 1.sp
            )
            pText(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = accentColor,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun HistoricalLogItem(record: ScoreRecord) {
    val redDanger = Color(0xFFEF4444)
    val greenNeon = Color(0xFF10B981)
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val statusText = if (record.escaped) "УШЕЛ" else "ПОЙМАН"
                    val statusColor = if (record.escaped) greenNeon else redDanger
                    pText(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = statusColor
                    )
                    pText(
                        text = "Очки: ${record.score}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Date stamp
                val dateStr = remember(record.timestamp) {
                    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                    sdf.format(Date(record.timestamp))
                }
                pText(
                    text = dateStr,
                    fontSize = 11.sp,
                    color = Color(0xFFA0A0AB)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                pText(
                    text = "Розыск: Lvl ${record.heatLevel}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                val durationStr = String.format("%02d:%02d", record.durationSeconds / 60, record.durationSeconds % 60)
                pText(
                    text = "Время: $durationStr",
                    fontSize = 11.sp,
                    color = Color(0xFFA0A0AB),
                    fontFamily = FontFamily.Monospace
                )
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
