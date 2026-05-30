package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PlayerRank(val title: String, val levelRequired: Int) {
    SH_6("Шестёрка", 1),
    STRELOK("Стрелок", 5),
    BOETS("Боец", 10),
    TORPEDA("Торпеда", 15),
    SMOTRYASHY("Смотрящий за Районом", 20),
    AVTORITET("Авторитет", 25),
    BRIGADIR("Бригадир", 30),
    VOR_V_ZAKONE("Вор в Законе", 35),
    CRIMINAL_GENERAL("Криминальный Генерал", 40),
    KOROL_RAYONA("Король Района", 45),
    KHOZYAIN_GORODA("Хозяин Города", 49),
    PAKHAN("Пахан", 50);

    companion object {
        fun fromLevel(level: Int): PlayerRank {
            return values().findLast { level >= it.levelRequired } ?: SH_6
        }
    }
}

@Entity(tableName = "car_config")
data class CarConfig(
    @PrimaryKey val id: Int = 1, // Store single player garage config
    val cash: Int = 55000,       // Starting cash set to $55,000 as requested
    val engineLevel: Int = 1,    // Speed & acceleration, Max 5
    val tyresLevel: Int = 1,     // Handling & drift grip, Max 5
    val brakesLevel: Int = 1,    // Friction & stop power, Max 5
    val nitroLevel: Int = 1,     // Nitro capacity & fill rate, Max 5
    val carColor: Int = 0xFF18181C.toInt(), // Hex color of the car (default black Lada)

    // Slavic RPG Elements
    val nickname: String = "mikha_q",
    val level: Int = 1,
    val experience: Int = 0,
    val weaponLevel: Int = 0,    // 0 = None, 1 = PM Pistol, 2 = AK-74u, 3 = RPG-7
    val familyLevel: Int = 0,    // 0 = Alone, 1 = Gopnik Crew, 2 = Bratva Gang, 3 = Full Syndicate
    val targetWeather: String = "OVERCAST", // OVERCAST, RAIN, SNOW
    val godMode: Boolean = false,
    val bigSpoiler: Boolean = true, // Lada sports spoiler enabled
    val neonUnderglow: Boolean = true // Neon green glow behind Lada
)

@Entity(tableName = "score_records")
data class ScoreRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val score: Int,
    val durationSeconds: Int,
    val heatLevel: Int,
    val escaped: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
