package com.example.data.local

import android.content.Context
import androidx.room.*
import com.example.data.model.CarConfig
import com.example.data.model.ScoreRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface CarConfigDao {
    @Query("SELECT * FROM car_config WHERE id = 1")
    fun getCarConfig(): Flow<CarConfig?>

    @Query("SELECT * FROM car_config WHERE id = 1")
    suspend fun getCarConfigSync(): CarConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCarConfig(config: CarConfig)
}

@Dao
interface ScoreRecordDao {
    @Query("SELECT * FROM score_records ORDER BY timestamp DESC")
    fun getAllScores(): Flow<List<ScoreRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: ScoreRecord)
}

@Database(entities = [CarConfig::class, ScoreRecord::class], version = 1, exportSchema = false)
abstract class GameDatabase : RoomDatabase() {
    abstract fun carConfigDao(): CarConfigDao
    abstract fun scoreRecordDao(): ScoreRecordDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getDatabase(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "car_chase_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
