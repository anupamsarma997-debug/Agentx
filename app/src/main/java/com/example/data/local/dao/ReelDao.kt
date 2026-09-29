package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ReelDraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReelDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReel(reel: ReelDraftEntity)

    @Update
    suspend fun updateReel(reel: ReelDraftEntity)

    @Query("SELECT * FROM reel_drafts WHERE id = :id LIMIT 1")
    suspend fun getReelById(id: String): ReelDraftEntity?

    @Query("SELECT * FROM reel_drafts ORDER BY createdAt DESC")
    fun getAllReelsFlow(): Flow<List<ReelDraftEntity>>

    @Query("SELECT * FROM reel_drafts WHERE generationStatus = :status ORDER BY createdAt DESC")
    fun getReelsByStatus(status: String): Flow<List<ReelDraftEntity>>

    @Query("SELECT COUNT(*) FROM reel_drafts")
    fun getReelCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM reel_drafts WHERE generationStatus = :status")
    fun getReelCountByStatus(status: String): Flow<Int>

    @Query("SELECT * FROM reel_drafts WHERE contentHash = :hash LIMIT 1")
    suspend fun getReelByContentHash(hash: String): ReelDraftEntity?

    @Query("UPDATE reel_drafts SET generationStatus = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE reel_drafts SET hook = :hook, caption = :caption, voiceover = :voiceover, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateReelTexts(
        id: String,
        hook: String,
        caption: String,
        voiceover: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM reel_drafts WHERE id = :id")
    suspend fun deleteReelById(id: String)

    @Delete
    suspend fun deleteReel(reel: ReelDraftEntity)
}
