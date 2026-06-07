package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_stats")
data class PlayerStats(
    @PrimaryKey val id: Int = 1,
    val nickname: String = "Siberian_Drifter",
    val cash: Int = 15000,
    val experience: Int = 0,
    val level: Int = 1,
    val highscore: Int = 0,
    val totalRuns: Int = 0,
    
    // Upgrades
    val engineLevel: Int = 1,
    val nitroLevel: Int = 1,
    val armorLevel: Int = 1,
    val neonThemeId: Int = 0 // 0 = None, 1 = Cyan Drift, 2 = Pink Plasma, 3 = Tokyo Gold
) {
    val rankTitle: String
        get() = when {
            level >= 15 -> "Авторитет"
            level >= 10 -> "Пахан"
            level >= 7  -> "Бригадир"
            level >= 4  -> "Бывалый"
            else        -> "Новичок"
        }
}
