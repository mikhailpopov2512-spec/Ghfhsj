package com.example.data.local

import android.content.Context
import androidx.room.*
import com.example.data.model.PlayerStats
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerStatsDao {
    @Query("SELECT * FROM player_stats WHERE id = 1 LIMIT 1")
    fun getStatsFlow(): Flow<PlayerStats?>

    @Query("SELECT * FROM player_stats WHERE id = 1 LIMIT 1")
    suspend fun getStats(): PlayerStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: PlayerStats)
}

@Database(entities = [PlayerStats::class], version = 1, exportSchema = false)
abstract class GameDatabase : RoomDatabase() {
    abstract val dao: PlayerStatsDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getInstance(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "carchase_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
