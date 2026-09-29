package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.MemeDraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeme(meme: MemeDraftEntity): Long

    @Update
    suspend fun updateMeme(meme: MemeDraftEntity)

    @Query("SELECT * FROM meme_drafts ORDER BY createdAt DESC")
    fun getAllMemes(): Flow<List<MemeDraftEntity>>

    @Query("SELECT * FROM meme_drafts WHERE generationStatus = :status ORDER BY createdAt DESC")
    fun getMemesByStatus(status: String): Flow<List<MemeDraftEntity>>

    @Query("SELECT * FROM meme_drafts WHERE safetyStatus = :safetyStatus ORDER BY createdAt DESC")
    fun getMemesBySafetyStatus(safetyStatus: String): Flow<List<MemeDraftEntity>>

    @Query("SELECT * FROM meme_drafts WHERE id = :id LIMIT 1")
    fun getMemeById(id: String): Flow<MemeDraftEntity?>

    @Query("SELECT * FROM meme_drafts WHERE id = :id LIMIT 1")
    suspend fun getMemeByIdSync(id: String): MemeDraftEntity?

    @Query("SELECT * FROM meme_drafts WHERE contentHash = :hash LIMIT 1")
    suspend fun getMemeByContentHash(hash: String): MemeDraftEntity?

    @Query("UPDATE meme_drafts SET generationStatus = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE meme_drafts SET safetyStatus = :safetyStatus, generationStatus = :generationStatus, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateSafetyAndGenerationStatus(
        id: String,
        safetyStatus: String,
        generationStatus: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("UPDATE meme_drafts SET setupText = :setupText, punchlineText = :punchlineText, caption = :caption, hashtags = :hashtags, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateMemeDraftTexts(
        id: String,
        setupText: String,
        punchlineText: String,
        caption: String,
        hashtags: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query("DELETE FROM meme_drafts WHERE id = :id")
    suspend fun deleteMemeById(id: String)

    @Query("SELECT COUNT(*) FROM meme_drafts")
    fun countTotal(): Flow<Int>

    @Query("SELECT COUNT(*) FROM meme_drafts WHERE generationStatus = :status")
    fun countByGenerationStatus(status: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM meme_drafts WHERE safetyStatus = :status")
    fun countBySafetyStatus(status: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM meme_drafts WHERE createdAt >= :startOfDayMillis AND generationStatus IN ('DRAFT', 'APPROVED', 'REVIEW_REQUIRED')")
    fun countGeneratedSince(startOfDayMillis: Long): Flow<Int>
}
