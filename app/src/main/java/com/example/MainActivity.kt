package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.GameViewModel
import com.example.ui.Screen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.TuningScreen
import com.example.ui.theme.CarChaseTheme

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Enable Full-depth Edge to Edge visual curves
        enableEdgeToEdge()

        setContent {
            CarChaseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val currentScreen by viewModel.currentScreen.collectAsState()

                    // AnimatedContent transition for glossy screen shifts
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                        },
                        label = "screen_navigation"
                    ) { screen ->
                        when (screen) {
                            Screen.MainMenu -> MainMenuScreen(
                                viewModel = viewModel,
                                onNavigateToGarage = { viewModel.navigateTo(Screen.Tuning) },
                                onNavigateToStats = { viewModel.navigateTo(Screen.Stats) },
                                onNavigateToSettings = { viewModel.navigateTo(Screen.Settings) },
                                onStartGame = { viewModel.navigateTo(Screen.Game) }
                            )
                            Screen.Game -> GameScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.navigateTo(Screen.MainMenu) }
                            )
                            Screen.Tuning -> TuningScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.navigateTo(Screen.MainMenu) }
                            )
                            Screen.Stats -> StatsScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.navigateTo(Screen.MainMenu) }
                            )
                            Screen.Settings -> SettingsScreen(
                                viewModel = viewModel,
                                onBack = { viewModel.navigateTo(Screen.MainMenu) }
                            )
                        }
                    }
                }
            }
        }
    }
}
