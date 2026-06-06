package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GameViewModel

@Composable
fun SettingsScreen(
    viewModel: GameViewModel,
    onBackToMenu: () -> Unit
) {
    val carConfig by viewModel.carConfigState.collectAsState()

    // Vibrant Palette Theme Colors
    val slateBG = Color(0xFF0F172A)
    val slateCard = Color(0xFF1E293B)
    val blueAccent = Color(0xFF3B82F6)
    val yellowGold = Color(0xFFFFD700)
    val greenNeon = Color(0xFF10B981)
    val orangeWarn = Color(0xFFF97316)
    val redDanger = Color(0xFFEF4444)

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
            // Header Bar
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
                            .testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "НАСТРОЙКИ РАЙОНА",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f).padding(end = 48.dp)
                    )
                }
            }

            // Description / Information Item
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = slateCard.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = blueAccent,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Конфигурируй физику, плотность патрулей ДПС, уровень тонировки стёкол и параметры дорожного полотна перед выездом в город.",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8),
                            fontFamily = FontFamily.SansSerif,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Category 1: WORLD / CITY CONFIG
            item {
                CategoryHeading(text = "ПАРАМЕТРЫ ОКРУЖАЮЩЕГО МИРА")
            }

            // Map Size Selector
            item {
                SegmentedSelector(
                    title = "РАЗМЕР КАРТЫ ГОРОДА",
                    subtitle = "Масштабирует площадь районов и количество гаражей",
                    options = listOf("NORMAL" to "Нормальный", "BIG" to "Большой", "ULTRA" to "Ультра-Хаос"),
                    selectedOption = carConfig.mapSizeSetting,
                    onSelected = { viewModel.updateMapSize(it) },
                    testTagPrefix = "map_size"
                )
            }

            // Weather Target
            item {
                SegmentedSelector(
                    title = "ПОГОДА / АТМОСФЕРА",
                    subtitle = "Влияет на сцепление шин с асфальтом (заносы и скольжение)",
                    options = listOf("OVERCAST" to "Пасмурно", "RAIN" to "Ливень", "SNOW" to "Снегопад"),
                    selectedOption = carConfig.targetWeather,
                    onSelected = { viewModel.updateWeatherFromSettings(it) },
                    testTagPrefix = "weather"
                )
            }

            // Traffic Density Selector (COP DENSITY)
            item {
                SegmentedSelectorInt(
                    title = "ОБИЛИЕ БОТОВ ПАТРУЛЯ ДПС",
                    subtitle = "Регулирует количество и частоту появления преследователей",
                    options = listOf(1 to "Мало", 2 to "Обычный", 3 to "ЖЕСТЬ (Хаос)"),
                    selectedOption = carConfig.copDensitySetting,
                    onSelected = { viewModel.updateCopDensity(it) },
                    testTagPrefix = "traffic_density"
                )
            }

            // Category 2: ADVANCED VEHICLE TUNING / TINT & SUSPENSION
            item {
                CategoryHeading(text = "КОСМЕТИЧЕСКИЙ ТЮНИНГ И СТИЛЬ")
            }

            // Tint level selector
            item {
                SegmentedSelectorInt(
                    title = "УРОВЕНЬ ТОНИРОВКИ СТЁКОЛ",
                    subtitle = "Эстетика салона автомобиля и скрытность от патрулей",
                    options = listOf(0 to "Без тонировки", 1 to "Только сзади", 2 to "В Хлам (В бункер!)"),
                    selectedOption = carConfig.tintLevel,
                    onSelected = { viewModel.updateTintLevel(it) },
                    testTagPrefix = "tint_level"
                )
            }

            // Suspension height selector
            item {
                SegmentedSelectorInt(
                    title = "КЛИРЕНС ПОДВЕСКИ",
                    subtitle = "Влияет на устойчивость в заносе крутых поворотов",
                    options = listOf(0 to "Заниженная LADA-Low", 1 to "Заводской клиренс", 2 to "Высокий Вездеход"),
                    selectedOption = carConfig.suspensionHeight,
                    onSelected = { viewModel.updateSuspensionHeight(it) },
                    testTagPrefix = "suspension_height"
                )
            }

            // Neon Underglow Colors
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = slateCard),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "ЦВЕТ НЕОНОВОЙ ПОДСВЕТКИ ДНИЩА",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            fontFamily = FontFamily.Monospace
                        )

                        val neons = listOf(
                            0xFF10B981 to "Зеленый",
                            0xFFEF4444 to "Пламя Red",
                            0xFF3B82F6 to "Космос Blue",
                            0xFFEC4899 to "Маджента",
                            0xFFF97316 to "Оранж",
                            0xFFEAB308 to "Янтарь"
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            neons.forEachIndexed { idx, item ->
                                val hexVal = item.first
                                val isSelected = carConfig.neonColorHex == hexVal
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color(hexVal))
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { viewModel.updateNeonColorHex(hexVal) }
                                        .testTag("neon_color_select_$idx"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Extra details / Footer Info
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Gear",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Версия конфигурации: v4.2.0-LADA",
                        fontSize = 11.sp,
                        color = Color(0xFF475569),
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun CategoryHeading(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF3B82F6),
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun SegmentedSelector(
    title: String,
    subtitle: String,
    options: List<Pair<String, String>>,
    selectedOption: String,
    onSelected: (String) -> Unit,
    testTagPrefix: String
) {
    val slateCard = Color(0xFF1E293B)
    Card(
        colors = CardDefaults.cardColors(containerColor = slateCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF475569),
                    lineHeight = 15.sp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                options.forEach { (optionKey, optionLabel) ->
                    val isSelected = selectedOption == optionKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Color(0xFF3B82F6) else Color.Transparent)
                            .clickable { onSelected(optionKey) }
                            .testTag("${testTagPrefix}_btn_$optionKey"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = optionLabel,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SegmentedSelectorInt(
    title: String,
    subtitle: String,
    options: List<Pair<Int, String>>,
    selectedOption: Int,
    onSelected: (Int) -> Unit,
    testTagPrefix: String
) {
    val slateCard = Color(0xFF1E293B)
    Card(
        colors = CardDefaults.cardColors(containerColor = slateCard),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF475569),
                    lineHeight = 15.sp
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F172A)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                options.forEach { (optionKey, optionLabel) ->
                    val isSelected = selectedOption == optionKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) Color(0xFF3B82F6) else Color.Transparent)
                            .clickable { onSelected(optionKey) }
                            .testTag("${testTagPrefix}_btn_$optionKey"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = optionLabel,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
