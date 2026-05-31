package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GameViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TuningScreen(
    viewModel: GameViewModel,
    onBackToMenu: () -> Unit
) {
    val config by viewModel.carConfigState.collectAsState()
    val garageCategory = remember { mutableStateOf(0) } // 0 = ВСЕ, 1 = СССР/РФ, 2 = ИНОМАРКИ, 3 = ТЯЖЕЛЫЕ

    // Vibrant Palette Theme Colors
    val slateBG = Color(0xFF0F172A)
    val slateCard = Color(0xFF1E293B)
    val blueAccent = Color(0xFF3B82F6)
    val yellowGold = Color(0xFFFFD700)
    val greenNeon = Color(0xFF10B981)

    val engineLevel = config.engineLevel
    val tyresLevel = config.tyresLevel
    val brakesLevel = config.brakesLevel
    val nitroLevel = config.nitroLevel
    val playerCash = config.cash

    // Upgrade costs
    val engineCost = engineLevel * 500
    val tyresCost = tyresLevel * 450
    val brakesCost = brakesLevel * 350
    val nitroCost = nitroLevel * 400

    val paintColors = listOf(
        Pair("Чёрный Уголь", 0xFF0F0F13.toInt()), // Classic original black lada
        Pair("Алая Искра", 0xFFE02B2B.toInt()),
        Pair("Золото ОПГ", 0xFFFFB900.toInt()),
        Pair("Синий Космос", 0xFF0030FF.toInt()),
        Pair("Кибер Неон", 0xFF35FF14.toInt()),
        Pair("Сталь Самары", 0xFF8E8E93.toInt())
    )

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
                            .testTag("back_to_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    pText(
                        text = "ТЮНИНГ-ПРО АВТОСЕРВИС",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )

                    // Display Cash balance in rubles
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(slateCard)
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(30.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        pText(text = "₽", color = greenNeon, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        pText(
                            text = NumberFormat.getNumberInstance(Locale.US).format(playerCash),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Interactive Dynamic Car Wireframe Card representing Lada Samara
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = slateCard),
                    border = BorderStroke(2.dp, blueAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val modelProjectName = when (config.carModelIndex) {
                            0 -> "ИНЖЕНЕРНЫЙ ПРОЕКТ: ВАЗ-2106 «КЛАССИКА»"
                            1 -> "ИНЖЕНЕРНЫЙ ПРОЕКТ: ВАЗ-2114 «ЧЕТЫРКА»"
                            2 -> "ИНЖЕНЕРНЫЙ ПРОЕКТ: LADA PRIORA «ЗАНИЖЕННАЯ»"
                            3 -> "ИНЖЕНЕРНЫЙ ПРОЕКТ: КАМАЗ-54115 «ГРОМОБОЙ»"
                            4 -> "ИНЖЕНЕРНЫЙ ПРОЕКТ: BMW E34 «БУМЕР ОПГ»"
                            else -> "ИНЖЕНЕРНЫЙ ПРОЕКТ: КИБЕРКОРЧ"
                        }
                        pText(
                            text = modelProjectName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFA0A0AB),
                            letterSpacing = 1.2.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Draw a vector sports car dynamically using Canvas matching selected Custom paint color
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            val w = size.width
                            val h = size.height

                            // Draw futuristic grid behind the car wireframe
                            val colCount = 10
                            for (j in 0..colCount) {
                                val gridX = (w / colCount) * j
                                drawLine(
                                    color = Color(0x13FFFFFF),
                                    start = Offset(gridX, 0f),
                                    end = Offset(gridX, h),
                                    strokeWidth = 1f
                                )
                            }
                            val rowCount = 6
                            for (j in 0..rowCount) {
                                val gridY = (h / rowCount) * j
                                drawLine(
                                    color = Color(0x13FFFFFF),
                                    start = Offset(0f, gridY),
                                    end = Offset(w, gridY),
                                    strokeWidth = 1f
                                )
                            }

                             // Dynamic Car Body drawing paths
                             val model = com.example.data.model.CarCatalog.models.getOrNull(config.carModelIndex) ?: com.example.data.model.CarCatalog.models[0]
                             val carBrushColor = Color(config.carColor)
                             val carOutlinePath = Path().apply {
                                 when (model.interiorType) {
                                     3 -> { // КАМАЗ / Heavy boxy cab-over truck outline!
                                         moveTo(w * 0.15f, h * 0.72f) // bottom rear floor
                                         lineTo(w * 0.15f, h * 0.15f) // heavy container top rear
                                         lineTo(w * 0.60f, h * 0.15f) // heavy container top front
                                         lineTo(w * 0.60f, h * 0.32f) // back of cabin drop
                                         lineTo(w * 0.86f, h * 0.32f) // truck high cabin top
                                         lineTo(w * 0.86f, h * 0.72f) // vertical flat truck front grill
                                         close()
                                     }
                                     0 -> { // ВАЗ-2106 (Sedan classic)
                                         moveTo(w * 0.15f, h * 0.68f) // rear floor
                                         lineTo(w * 0.15f, h * 0.50f) // trunk start
                                         lineTo(w * 0.30f, h * 0.50f) // hood/trunk line
                                         lineTo(w * 0.40f, h * 0.35f) // rear glass window pitch
                                         lineTo(w * 0.64f, h * 0.35f) // flat roof
                                         lineTo(w * 0.72f, h * 0.50f) // windshield pitch
                                         lineTo(w * 0.86f, h * 0.50f) // flat vertical front hood
                                         lineTo(w * 0.86f, h * 0.68f) // front bumper
                                         close()
                                     }
                                     2 -> { // Lada Priora (Slammed modern low profile sedan)
                                         moveTo(w * 0.13f, h * 0.73f) // slammed low floor
                                         lineTo(w * 0.15f, h * 0.53f) // low trunk
                                         lineTo(w * 0.32f, h * 0.53f) 
                                         lineTo(w * 0.43f, h * 0.33f) // sleek cabin
                                         lineTo(w * 0.63f, h * 0.33f) // roof
                                         lineTo(w * 0.74f, h * 0.51f) // windscreen
                                         lineTo(w * 0.87f, h * 0.71f) // low hood
                                         lineTo(w * 0.87f, h * 0.73f) // slammed bumper
                                         close()
                                     }
                                     4 -> { // BMW E34 / Foreign Sport (Sleek bandit sport sedan, aggressive front nose)
                                         moveTo(w * 0.14f, h * 0.68f)
                                         lineTo(w * 0.16f, h * 0.48f) // wing
                                         lineTo(w * 0.33f, h * 0.48f) // trunk
                                         lineTo(w * 0.42f, h * 0.29f) // lean screen
                                         lineTo(w * 0.63f, h * 0.29f) // low roof
                                         lineTo(w * 0.72f, h * 0.46f) // sloped front screen
                                         lineTo(w * 0.84f, h * 0.46f) // long hood
                                         lineTo(w * 0.86f, h * 0.50f) // double kidney grill nose tilt
                                         lineTo(w * 0.86f, h * 0.68f) // chin spoiler
                                         close()
                                     }
                                     else -> { // ВАЗ-2114 Hatchback default
                                         moveTo(w * 0.15f, h * 0.68f)
                                         lineTo(w * 0.24f, h * 0.42f) // steep hatchback tail
                                         lineTo(w * 0.42f, h * 0.30f) // cabin flat roof
                                         lineTo(w * 0.62f, h * 0.30f)
                                         lineTo(w * 0.73f, h * 0.50f) // windshield
                                         lineTo(w * 0.86f, h * 0.52f) // hood
                                         lineTo(w * 0.86f, h * 0.68f) // front bumper
                                         close()
                                     }
                                 }
                             }

                            // Draw Neon Green Underglow glowing reflection if equipped!
                            if (config.neonUnderglow) {
                                drawLine(
                                    color = greenNeon.copy(alpha = 0.84f),
                                    start = Offset(w * 0.22f, h * 0.75f),
                                    end = Offset(w * 0.80f, h * 0.75f),
                                    strokeWidth = 10f
                                )
                            }

                            // Draw car body fill
                            drawPath(
                                path = carOutlinePath,
                                color = carBrushColor.copy(alpha = 0.3f)
                            )
                            // Draw car body outlines
                            drawPath(
                                path = carOutlinePath,
                                color = carBrushColor,
                                style = Stroke(width = 4f)
                            )

                            // Aerodynamic Big Spoiler block
                            if (config.bigSpoiler) {
                                drawLine(
                                    color = Color.White,
                                    start = Offset(w * 0.13f, h * 0.38f),
                                    end = Offset(w * 0.22f, h * 0.38f),
                                    strokeWidth = 6f
                                )
                                drawLine(
                                    color = carBrushColor,
                                    start = Offset(w * 0.17f, h * 0.38f),
                                    end = Offset(w * 0.17f, h * 0.45f),
                                    strokeWidth = 3f
                                )
                            }

                            // Wheels
                            drawCircle(
                                color = Color(0xFF141419),
                                radius = 22f,
                                center = Offset(w * 0.30f, h * 0.70f)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 20f,
                                center = Offset(w * 0.30f, h * 0.70f),
                                style = Stroke(width = 3f)
                            )
                            drawCircle(
                                color = Color(0xFF141419),
                                radius = 22f,
                                center = Offset(w * 0.72f, h * 0.70f)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 20f,
                                center = Offset(w * 0.72f, h * 0.70f),
                                style = Stroke(width = 3f)
                            )

                            // Glass window interior
                            val windowPath = Path().apply {
                                moveTo(w * 0.46f, h * 0.32f)
                                lineTo(w * 0.60f, h * 0.32f)
                                lineTo(w * 0.68f, h * 0.48f)
                                lineTo(w * 0.40f, h * 0.48f)
                                close()
                            }
                            drawPath(
                                path = windowPath,
                                color = Color(0xFF00D2FF).copy(alpha = 0.5f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            pText(
                                text = "Neon Underglow: " + if (config.neonUnderglow) "ACTIVE ✅" else "OFF",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (config.neonUnderglow) greenNeon else Color.Gray
                            )
                            pText(
                                text = "Big Spoiler: " + if (config.bigSpoiler) "ACTIVE ✅" else "OFF",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (config.bigSpoiler) blueAccent else Color.Gray
                            )
                        }
                    }
                }
            }

            // Car Model Selector & Shop Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = slateCard),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        pText(
                            text = "АВТОПАРК (УПРАВЛЕНИЕ ГАРАЖОМ)",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = Color.White,
                            letterSpacing = 1.0.sp
                        )

                        // 3. Category Filter Chips (СРРФ / Иномарки / Тяжёлые / Все)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val categories = listOf("ВСЕ", "СССР / РФ", "ИНОМАРКИ", "ТЯЖЁЛЫЕ")
                            categories.forEachIndexed { catIdx, label ->
                                val isCatSelected = garageCategory.value == catIdx
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isCatSelected) greenNeon.copy(alpha = 0.25f) else Color(0xFF0F172A))
                                        .border(1.dp, if (isCatSelected) greenNeon else Color(0xFF334155), RoundedCornerShape(8.dp))
                                        .clickable { garageCategory.value = catIdx }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    pText(
                                        text = label,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCatSelected) greenNeon else Color.White
                                    )
                                }
                            }
                        }

                        // Use the global CarCatalog containing exactly 52 cars!
                        val displayedCarsWithIndex = com.example.data.model.CarCatalog.models.mapIndexed { index, model -> index to model }
                            .filter { (carIdx, model) ->
                                when (garageCategory.value) {
                                    1 -> (carIdx in 0..18) || (carIdx in 27..38)
                                    2 -> (carIdx in 19..26) || (carIdx in 39..51)
                                    3 -> model.interiorType == 3
                                    else -> true
                                }
                            }

                        displayedCarsWithIndex.forEach { (carIdx, model) ->
                            val name = model.name
                            val price = model.price
                            val desc = model.description
                            
                            val isSelected = config.carModelIndex == carIdx
                            val canAfford = config.cash >= price

                            val borderCol = if (isSelected) greenNeon else if (canAfford) Color(0xFF334155) else Color(0x52EF4444)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Color(0x1F10B981) else Color(0xFF0F172A))
                                    .border(1.dp, borderCol, RoundedCornerShape(10.dp))
                                    .clickable {
                                        if (isSelected) {
                                            // already active
                                        } else if (canAfford) {
                                            viewModel.purchaseCarModel(carIdx, price)
                                        } else {
                                            // Simply allow select or buy
                                            viewModel.purchaseCarModel(carIdx, price)
                                        }
                                    }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1.5f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically, 
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            pText(
                                                text = name,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) greenNeon else Color.White,
                                                fontSize = 13.sp
                                            )
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(greenNeon.copy(alpha = 0.2f))
                                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                                ) {
                                                    pText("ДОСТУПЕН", fontSize = 8.sp, color = greenNeon, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        pText(
                                            text = desc,
                                            color = Color(0xFFA0A0AB),
                                            fontSize = 10.sp
                                        )
                                    }

                                    Column(
                                        horizontalAlignment = Alignment.End,
                                        modifier = Modifier.weight(0.8f)
                                    ) {
                                        if (price == 0) {
                                            pText(text = "БЕСПЛАТНО", color = greenNeon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        } else {
                                            pText(
                                                text = "${NumberFormat.getNumberInstance(Locale.US).format(price)} ₽",
                                                color = if (canAfford) yellowGold else Color(0xFFEF4444),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Exclusive Parts customization (Underglow & Spoiler Toggles)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = slateCard),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pText(
                            text = "АТЕЛЬЕ ОСОБЫХ ДЕТАЛЕЙ",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Underglow purchase / toggle
                            Button(
                                onClick = {
                                    viewModel.toggleNeonUnderglow()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x1810B981)),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, greenNeon),
                                modifier = Modifier.weight(1f)
                            ) {
                                pText(text = "Зелёный Неон", color = greenNeon, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Spoiler purchase / toggle
                            Button(
                                onClick = {
                                    viewModel.toggleSpoiler()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0x183B82F6)),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, blueAccent),
                                modifier = Modifier.weight(1f)
                            ) {
                                pText(text = "Спойлер Спорт", color = blueAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Upgrades List
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TuningUpgradeRow(
                        title = "МЕДНЫЙ ДВИГАТЕЛЬ (RACER EX)",
                        subtitle = "Увеличивает ускорение и пиковую скорость Lada",
                        level = engineLevel,
                        cost = engineCost,
                        playerCash = playerCash,
                        onUpgrade = { viewModel.upgradeEngine() },
                        tag = "upgrade_engine"
                    )

                    TuningUpgradeRow(
                        title = "ДРИФТ-ШИНЫ СЛИКИ",
                        subtitle = "Повышает управляемость и снижает износ на поворотах",
                        level = tyresLevel,
                        cost = tyresCost,
                        playerCash = playerCash,
                        onUpgrade = { viewModel.upgradeTyres() },
                        tag = "upgrade_tyres"
                    )

                    TuningUpgradeRow(
                        title = "КЕРАМИЧЕСКИЙ СУППОРТ",
                        subtitle = "Мгновенное сокращение тормозного пути ДПС-заноса",
                        level = brakesLevel,
                        cost = brakesCost,
                        playerCash = playerCash,
                        onUpgrade = { viewModel.upgradeBrakes() },
                        tag = "upgrade_brakes"
                    )

                    TuningUpgradeRow(
                        title = "НАГНЕТАТЕЛЬ NITRO КРАСНЫЙ",
                        subtitle = "Увеличивает ёмкость балона закиси азота x2",
                        level = nitroLevel,
                        cost = nitroCost,
                        playerCash = playerCash,
                        onUpgrade = { viewModel.upgradeNitro() },
                        tag = "upgrade_nitro"
                    )
                }
            }

            // Paint customizing grid
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = slateCard),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        pText(
                            text = "ПОДКРАСКА И ЦВЕТ КУЗОВА",
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = Color.White
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            paintColors.forEach { (name, colorValue) ->
                                val isSelected = config.carColor == colorValue
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .aspectRatio(1f)
                                        .clip(CircleShape)
                                        .background(Color(colorValue))
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color.White else Color(0x35000000),
                                            shape = CircleShape
                                        )
                                        .clickable { viewModel.customizeColor(colorValue) }
                                )
                            }
                        }
                    }
                }
            }

            // Quick add cash for tuning trial simulations
            item {
                Button(
                    onClick = { viewModel.addCheatCash() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x18FFD700)),
                    modifier = Modifier
                        .testTag("cheat_cash_garage_btn")
                        .padding(top = 10.dp),
                    shape = RoundedCornerShape(30.dp),
                    border = BorderStroke(1.dp, yellowGold.copy(alpha = 0.5f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddCircle,
                            contentDescription = "Coin Add",
                            tint = yellowGold,
                            modifier = Modifier.size(18.dp)
                        )
                        pText(
                            text = "ОФФШОРНЫЙ ГРАНТ: +5,000 ₽",
                            color = yellowGold,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun TuningUpgradeRow(
    title: String,
    subtitle: String,
    level: Int,
    cost: Int,
    playerCash: Int,
    onUpgrade: () -> Unit,
    tag: String
) {
    val greenNeon = Color(0xFF10B981)
    val enabled = level < 5 && playerCash >= cost

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        border = BorderStroke(1.dp, Color(0xFF334155)),
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
                Column(modifier = Modifier.weight(1f)) {
                    pText(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    pText(text = subtitle, fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    pText(text = "Lvl $level/5", fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (level < 5) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        pText(text = " ₽", color = greenNeon, fontWeight = FontWeight.Bold)
                        pText(text = String.format("%,d", cost), fontSize = 13.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                    }
                    Button(
                        onClick = onUpgrade,
                        enabled = enabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0284C7),
                            disabledContainerColor = Color(0xFF334155)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp).testTag(tag)
                    ) {
                        pText(
                            text = if (playerCash >= cost) "КУПИТЬ" else "НЕТ СРЕДСТВ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (enabled) Color.White else Color.Gray
                        )
                    }
                } else {
                    pText(text = "ДЕКАЛЬ МАКСИМУМ ⭐️", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
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
