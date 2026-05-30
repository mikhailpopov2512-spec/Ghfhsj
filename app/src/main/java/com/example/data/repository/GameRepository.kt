package com.example.data.repository

import com.example.data.local.CarConfigDao
import com.example.data.local.ScoreRecordDao
import com.example.data.model.CarConfig
import com.example.data.model.ScoreRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(
    private val carConfigDao: CarConfigDao,
    private val scoreRecordDao: ScoreRecordDao
) {
    val carConfig: Flow<CarConfig> = carConfigDao.getCarConfig()
        .map { it ?: CarConfig() }

    val allScores: Flow<List<ScoreRecord>> = scoreRecordDao.getAllScores()

    suspend fun saveCarConfig(config: CarConfig) {
        carConfigDao.saveCarConfig(config)
    }

    suspend fun addCash(amount: Int) {
        val current = carConfigDao.getCarConfigSync() ?: CarConfig()
        val updated = current.copy(cash = current.cash + amount)
        carConfigDao.saveCarConfig(updated)
    }

    suspend fun spendCash(amount: Int): Boolean {
        val current = carConfigDao.getCarConfigSync() ?: CarConfig()
        if (current.cash < amount) return false
        val updated = current.copy(cash = current.cash - amount)
        carConfigDao.saveCarConfig(updated)
        return true
    }

    suspend fun insertScore(score: ScoreRecord) {
        scoreRecordDao.insertScore(score)
    }
}
