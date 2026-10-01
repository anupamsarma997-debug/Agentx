package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.AppLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: AppLogEntity): Long

    @Query("SELECT * FROM app_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogs(limit: Int = 100): Flow<List<AppLogEntity>>

    @Query("SELECT * FROM app_logs WHERE level = :level ORDER BY timestamp DESC LIMIT :limit")
    fun getLogsByLevel(level: String, limit: Int = 100): Flow<List<AppLogEntity>>

    @Query("DELETE FROM app_logs WHERE id NOT IN (SELECT id FROM app_logs ORDER BY timestamp DESC LIMIT :keepCount)")
    suspend fun trimLogs(keepCount: Int = 200)

    @Query("DELETE FROM app_logs")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM app_logs")
    suspend fun getCount(): Int
}
