package com.example.data.repository

import com.example.data.local.PlayerStatsDao
import com.example.data.model.PlayerStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(private val dao: PlayerStatsDao) {

    val playerStats: Flow<PlayerStats> = dao.getStatsFlow().map { it ?: PlayerStats() }

    suspend fun getStats(): PlayerStats {
        return dao.getStats() ?: PlayerStats().also { dao.insertOrUpdate(it) }
    }

    suspend fun updateNickname(name: String) {
        val current = getStats()
        dao.insertOrUpdate(current.copy(nickname = name))
    }

    suspend fun addCash(amount: Int) {
        val current = getStats()
        dao.insertOrUpdate(current.copy(cash = current.cash + amount))
    }

    suspend fun spendCash(amount: Int): Boolean {
        val current = getStats()
        if (current.cash >= amount) {
            dao.insertOrUpdate(current.copy(cash = current.cash - amount))
            return true
        }
        return false
    }

    suspend fun addExperience(xp: Int): Boolean {
        val current = getStats()
        var newXp = current.experience + xp
        var newLevel = current.level
        var leveledUp = false
        
        while (newXp >= 100) {
            newXp -= 100
            newLevel += 1
            leveledUp = true
        }
        
        dao.insertOrUpdate(current.copy(experience = newXp, level = newLevel))
        return leveledUp
    }

    suspend fun saveRunScore(score: Int) {
        val current = getStats()
        val newHigh = if (score > current.highscore) score else current.highscore
        dao.insertOrUpdate(
            current.copy(
                highscore = newHigh,
                totalRuns = current.totalRuns + 1
            )
        )
    }

    suspend fun upgradeEngine(): Boolean {
        val current = getStats()
        val cost = current.engineLevel * 5000
        if (current.engineLevel < 5 && spendCash(cost)) {
            dao.insertOrUpdate(current.copy(engineLevel = current.engineLevel + 1))
            return true
        }
        return false
    }

    suspend fun upgradeNitro(): Boolean {
        val current = getStats()
        val cost = current.nitroLevel * 4000
        if (current.nitroLevel < 5 && spendCash(cost)) {
            dao.insertOrUpdate(current.copy(nitroLevel = current.nitroLevel + 1))
            return true
        }
        return false
    }

    suspend fun upgradeArmor(): Boolean {
        val current = getStats()
        val cost = current.armorLevel * 3500
        if (current.armorLevel < 5 && spendCash(cost)) {
            dao.insertOrUpdate(current.copy(armorLevel = current.armorLevel + 1))
            return true
        }
        return false
    }

    suspend fun buyNeonTheme(themeId: Int, cost: Int): Boolean {
        val current = getStats()
        if (spendCash(cost)) {
            dao.insertOrUpdate(current.copy(neonThemeId = themeId))
            return true
        }
        return false
    }

    suspend fun selectNeonTheme(themeId: Int) {
        val current = getStats()
        dao.insertOrUpdate(current.copy(neonThemeId = themeId))
    }
}
