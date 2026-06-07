package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.GameDatabase
import com.example.data.model.PlayerStats
import com.example.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    object MainMenu : Screen()
    object Game : Screen()
    object Tuning : Screen()
    object Stats : Screen()
    object Settings : Screen()
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GameRepository
    
    val playerStats: StateFlow<PlayerStats>
    
    private val _currentScreen = MutableStateFlow<Screen>(Screen.MainMenu)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _selectedMapIndex = MutableStateFlow(0)
    val selectedMapIndex: StateFlow<Int> = _selectedMapIndex.asStateFlow()

    fun selectMap(index: Int) {
        _selectedMapIndex.value = index
    }

    init {
        val database = GameDatabase.getInstance(application)
        repository = GameRepository(database.dao)
        playerStats = repository.playerStats.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlayerStats()
        )
        
        // Ensure initial stats exist in DB
        viewModelScope.launch {
            repository.getStats()
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun updateNickname(newName: String) {
        viewModelScope.launch {
            repository.updateNickname(newName)
        }
    }

    fun adminInjectCustomCash(amount: Int) {
        viewModelScope.launch {
            repository.addCash(amount)
        }
    }

    fun saveRunResult(score: Int, earnedCash: Int, earnedXp: Int) {
        viewModelScope.launch {
            repository.saveRunScore(score)
            repository.addCash(earnedCash)
            repository.addExperience(earnedXp)
        }
    }

    fun upgradeEngine(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.upgradeEngine()
            onResult(success)
        }
    }

    fun upgradeNitro(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.upgradeNitro()
            onResult(success)
        }
    }

    fun upgradeArmor(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.upgradeArmor()
            onResult(success)
        }
    }

    fun selectNeonTheme(themeId: Int) {
        viewModelScope.launch {
            repository.selectNeonTheme(themeId)
        }
    }

    fun buyNeonTheme(themeId: Int, cost: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.buyNeonTheme(themeId, cost)
            onResult(success)
        }
    }

    // Admin commands
    fun applyMaxUpgrades() {
        viewModelScope.launch {
            val stats = repository.getStats()
            val database = GameDatabase.getInstance(getApplication())
            database.dao.insertOrUpdate(
                stats.copy(
                    engineLevel = 5,
                    nitroLevel = 5,
                    armorLevel = 5,
                    neonThemeId = 3,
                    cash = stats.cash + 100000,
                    level = stats.level + 5
                )
            )
        }
    }

    fun resetStats() {
        viewModelScope.launch {
            val database = GameDatabase.getInstance(getApplication())
            database.dao.insertOrUpdate(PlayerStats())
        }
    }
}
