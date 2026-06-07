package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun SettingsScreen(
    viewModel: GameViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isSoundEnabled by remember { mutableStateOf(true) }
    var musicVolume by remember { mutableFloatStateOf(0.8f) }
    var useSteeringWheel by remember { mutableStateOf(false) }

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
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "⚙️ НАСТРОЙКИ СИМУЛЯТОРА",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )

                // Placeholder to balance
                Spacer(modifier = Modifier.width(44.dp))
            }

            // Settings Column Panel
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x660A0F1D))
                    .border(BorderStroke(1.2.dp, Color.White.copy(alpha = 0.15f)), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                Text(
                    text = "КОНФИГУРАЦИЯ СРЕДЫ",
                    color = Color(0xFFFF007F),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                // Sound toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Звуковые эффекты", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Рев моторов Lada, сирены ДПС РФ", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                    }

                    Switch(
                        checked = isSoundEnabled,
                        onCheckedChange = {
                            isSoundEnabled = it
                            Toast.makeText(context, if (it) "Звук включен" else "Звук выключен", Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF00F0FF),
                            checkedTrackColor = Color(0xFF00F0FF).copy(alpha = 0.3f)
                        )
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                // Music volume
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Громкость Радио", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("${(musicVolume * 100).toInt()}%", color = Color(0xFF00F0FF), fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.MusicNote, contentDescription = "music", tint = Color.White.copy(alpha = 0.5f))
                        
                        Slider(
                            value = musicVolume,
                            onValueChange = { musicVolume = it },
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFF007F),
                                activeTrackColor = Color(0xFFFF007F),
                                inactiveTrackColor = Color(0x33FFFFFF)
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Icon(imageVector = Icons.Filled.VolumeUp, contentDescription = "loud", tint = Color.White.copy(alpha = 0.5f))
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                // Controls configuration selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Виртуальный Руль управления", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Показывать круглый графический руль вместо стрелок", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                    }

                    Switch(
                        checked = useSteeringWheel,
                        onCheckedChange = {
                            useSteeringWheel = it
                            Toast.makeText(context, if (it) "Управление: Виртуальный Руль" else "Управление: Стрелки дрифта", Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFFF9F00),
                            checkedTrackColor = Color(0xFFFF9F00).copy(alpha = 0.3f)
                        )
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                // Reset profiles button
                Button(
                    onClick = {
                        viewModel.resetStats()
                        Toast.makeText(context, "Все данные игры стерты!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E121F)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .border(BorderStroke(1.dp, Color(0xFFFF007F)), RoundedCornerShape(12.dp))
                ) {
                    Text("СБРОСИТЬ ВСЮ ИСТОРИЮ ИГРЫ", color = Color(0xFFFF007F), fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
