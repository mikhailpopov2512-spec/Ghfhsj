package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.GameViewModel
import com.example.ui.screens.GameScreen
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.TuningScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(dynamicColor = false) {
                val navController = rememberNavController()
                val gameViewModel: GameViewModel = viewModel()

                val carConfigState by gameViewModel.carConfigState.collectAsState()
                val scoreHistoryState by gameViewModel.scoreHistoryState.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "menu",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // Main Menu Screen
                        composable("menu") {
                            MainMenuScreen(
                                carConfig = carConfigState,
                                scoreHistory = scoreHistoryState,
                                onStartGame = {
                                    gameViewModel.initiateGameBoot()
                                    navController.navigate("game")
                                },
                                onNavigateToGarage = {
                                    navController.navigate("garage")
                                },
                                onNavigateToStats = {
                                    navController.navigate("stats")
                                },
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                },
                                onAddCheatCash = {
                                    gameViewModel.addCheatCash()
                                },
                                viewModel = gameViewModel
                            )
                        }

                        // Settings Panel Configuration Screen
                        composable("settings") {
                            SettingsScreen(
                                viewModel = gameViewModel,
                                onBackToMenu = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // Racing Game Simulation Screen
                        composable("game") {
                            GameScreen(
                                viewModel = gameViewModel,
                                onBackToMenu = {
                                    gameViewModel.exitToMenu()
                                    navController.navigate("menu") {
                                        popUpTo("menu") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Custom Car Garage / Tuning Screen
                        composable("garage") {
                            TuningScreen(
                                viewModel = gameViewModel,
                                onBackToMenu = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // History Statistics / Achievements
                        composable("stats") {
                            StatsScreen(
                                viewModel = gameViewModel,
                                onBackToMenu = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
