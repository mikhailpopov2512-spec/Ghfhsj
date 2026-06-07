package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.GameViewModel

@Composable
fun TuningScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val stats by viewModel.playerStats.collectAsState()

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

        // Dark Overlay for higher contrast tuning panels
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
            // Header with Back Button
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
                        contentDescription = "Back to menu",
                        tint = Color.White
                    )
                }

                Text(
                    text = "🔧 ТЮНИНГ ГАРАЖ: СИБИРЬ",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )

                // Current balance badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xE60F172A))
                        .border(BorderStroke(1.dp, Color(0xFF00FF66).copy(alpha = 0.3f)), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "₽ ${String.format("%,d", stats.cash)}",
                        color = Color(0xFF00FF66),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Central tuning grid (translucent liquid glass panel)
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
                Text(
                    text = "ФОРСИРОВАНИЕ ДВИГАТЕЛЕЙ И КУЗОВА LADA",
                    color = Color(0xFFFF9F00),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                // ENGINE UPGRADE ROW
                val engineCost = stats.engineLevel * 5000
                TuningUpgradeItem(
                    title = "Усиленный Двигатель V8",
                    level = stats.engineLevel,
                    maxLevel = 5,
                    cost = engineCost,
                    icon = { Icon(Icons.Filled.Build, null, tint = Color(0xFF00F0FF)) },
                    color = Color(0xFF00F0FF),
                    onUpgrade = {
                        if (stats.engineLevel >= 5) {
                            Toast.makeText(context, "Двигатель уже на максимуме!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.upgradeEngine { success ->
                                if (success) {
                                    Toast.makeText(context, "🚀 Двигатель V8 форсирован!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Недостаточно денежных средств!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)

                // NITRO UPGRADE ROW
                val nitroCost = stats.nitroLevel * 4000
                TuningUpgradeItem(
                    title = "Закись Азота Blue-NOS",
                    level = stats.nitroLevel,
                    maxLevel = 5,
                    cost = nitroCost,
                    icon = { Icon(Icons.Filled.FlashOn, null, tint = Color(0xFFFF007F)) },
                    color = Color(0xFFFF007F),
                    onUpgrade = {
                        if (stats.nitroLevel >= 5) {
                            Toast.makeText(context, "Нитро закись на максимуме!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.upgradeNitro { success ->
                                if (success) {
                                    Toast.makeText(context, "🔥 Закись азота Blue-NOS установлена!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Недостаточно денежных средств!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)

                // ARMOR UPGRADE ROW
                val armorCost = stats.armorLevel * 3500
                TuningUpgradeItem(
                    title = "Таранящий Обвес и Бронеплиты Lada",
                    level = stats.armorLevel,
                    maxLevel = 5,
                    cost = armorCost,
                    icon = { Icon(Icons.Filled.Shield, null, tint = Color(0xFFFF9F00)) },
                    color = Color(0xFFFF9F00),
                    onUpgrade = {
                        if (stats.armorLevel >= 5) {
                            Toast.makeText(context, "Усиление кузова на максимуме!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.upgradeArmor { success ->
                                if (success) {
                                    Toast.makeText(context, "🛡️ Карбоновая защита установлена!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Недостаточно денежных средств!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                )

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)

                // NEON HIGHLIGHT THEMES (0=None, 1=Cyan Drift, 2=Pink Plasma, 3=Tokyo Gold)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "АКТИВНЫЙ ПОДКУЗОВНОЙ НЕОН (ПОДСВЕТКА)",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NeonOptionCard(
                            title = "НЕТ",
                            isSelected = stats.neonThemeId == 0,
                            color = Color.Gray,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.selectNeonTheme(0) }
                        )

                        NeonOptionCard(
                            title = "CYAN",
                            isSelected = stats.neonThemeId == 1,
                            color = Color(0xFF00F0FF),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.selectNeonTheme(1)
                                Toast.makeText(context, "Выбран Сибирский Циан неон!", Toast.LENGTH_SHORT).show()
                            }
                        )

                        NeonOptionCard(
                            title = "PINK",
                            isSelected = stats.neonThemeId == 2,
                            color = Color(0xFFFF007F),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.selectNeonTheme(2)
                                Toast.makeText(context, "Выбран Плазма Розовый неон!", Toast.LENGTH_SHORT).show()
                            }
                        )

                        NeonOptionCard(
                            title = "GOLD",
                            isSelected = stats.neonThemeId == 3,
                            color = Color(0xFFFF9F00),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.selectNeonTheme(3)
                                Toast.makeText(context, "Выбран Золотой Токио неон!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TuningUpgradeItem(
    title: String,
    level: Int,
    maxLevel: Int,
    cost: Int,
    icon: @Composable () -> Unit,
    color: Color,
    onUpgrade: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
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
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.15f))
                        .border(BorderStroke(1.dp, color.copy(alpha = 0.4f)), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }

                Column {
                    Text(text = title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Уровень $level / $maxLevel", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                }
            }

            if (level < maxLevel) {
                Button(
                    onClick = onUpgrade,
                    colors = ButtonDefaults.buttonColors(containerColor = color),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "₽ ${String.format("%,d", cost)}",
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x3300FF66))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("МАКСИМУМ", color = Color(0xFF00FF66), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 1..maxLevel) {
                val itemModifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))

                Box(
                    modifier = if (i <= level) {
                        itemModifier.background(
                            Brush.horizontalGradient(
                                colors = listOf(color, color.copy(alpha = 0.6f))
                            )
                        )
                    } else {
                        itemModifier.background(Color(0x33FFFFFF))
                    }
                )
            }
        }
    }
}

@Composable
fun NeonOptionCard(
    title: String,
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) color.copy(alpha = 0.25f) else Color(0x221E293B)
            )
            .border(
                BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) color else Color.White.copy(alpha = 0.15f)
                ),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isSelected) color else Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace
        )
    }
}
